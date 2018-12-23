(ns illuminepixels.network.websocket
  (:require [illuminepixels.utils :as utils]
            [missing.core :as miss]
            [clojure.core.async :as async]
            [illuminepixels.network.api :as napi]
            [clojure.edn :as edn]
            [taoensso.timbre :as logger]
            [ring.adapter.jetty9.websocket :as jws]))

(defn callbacks-for-chan [chan]
  {:write-failed
   (fn [e] (logger/error e) (async/close! chan))
   :write-success
   (fn [] (async/close! chan))})

(defn send-message! [ws data]
  (let [finished (async/promise-chan)]
    (jws/send! ws (pr-str data) (callbacks-for-chan finished))
    finished))

(defn on-connect [ws]
  (let [messages (napi/get-messages)]
    (async/go-loop []
      (when-some [msg (async/<! messages)]
        (async/<! (send-message! ws msg))
        (recur)))))

(defn on-error [ws e]
  (logger/error e))

(defn on-close [ws status reason]
  (let [[{:keys [messages subscriptions]}] (reset-vals! napi/*state* {})]
    (miss/quietly (async/close! messages))
    (doseq [sub (vals subscriptions)]
      (miss/quietly (async/close! sub)))))

(defn on-text [ws message]
  (let [closure     napi/*state*
        command     (edn/read-string message)
        protocol    (get command :protocol)
        transaction (get command :transaction)
        {:keys [messages subscriptions]} (deref closure)]
    (case protocol
      :request
      (let [response (napi/handle-request (:data command))]
        (async/put! messages {:data response :protocol protocol :transaction transaction}))
      :subscription
      (if (contains? subscriptions transaction)
        (when (get command :unsubscribe)
          (async/close! (get subscriptions transaction)))
        (when-some [response (napi/handle-subscribe (:data command))]
          (utils/on-close response (fn [] (swap! closure miss/dissoc-in [:subscriptions transaction])))
          (swap! closure assoc-in [:subscriptions transaction] response)
          (async/go-loop []
            (when-some [res (async/<! response)]
              (if (async/>! messages {:data res :protocol protocol :transaction transaction})
                (recur)
                (async/close! response))))))
      :push
      (napi/handle-push command))))

(defn on-bytes [& args]
  (throw (ex-info "Unsupported websocket method." {})))

(defn websocket-routes [req]
  (let [closure (atom (napi/new-state req))]
    (letfn [(mw [handler]
              (fn [& args]
                (binding [napi/*state* closure]
                  (apply handler args))))]
      (miss/map-vals mw
        {:on-connect on-connect
         :on-error   on-error
         :on-close   on-close
         :on-text    on-text
         :on-bytes   on-bytes}))))
