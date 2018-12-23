(ns illuminepixels.network.websocket
  (:require [illuminepixels.utils :as utils]
            [missing.core :as miss]
            [clojure.core.async :as async]
            [illuminepixels.network.api :as napi]
            [cognitect.transit :as transit]
            [taoensso.timbre :as logger]
            [ring.adapter.jetty9.websocket :as jws])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)))

(defn callbacks-for-chan [chan]
  {:write-failed
   (fn [e] (logger/error e) (async/close! chan))
   :write-success
   (fn [] (async/close! chan))})

(defn send-message! [ws data]
  (let [finished (async/promise-chan)]
    (let [output (ByteArrayOutputStream. 4096)
          writer (transit/writer output :json)]
      (transit/write writer data)
      (jws/send! ws (String. (.toByteArray output)) (callbacks-for-chan finished)))
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

(defn on-command [ws command]
  (let [closure     napi/*state*
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

(defn on-text [ws message]
  (let [stream (ByteArrayInputStream. (.getBytes message))
        reader (transit/reader stream :json)]
    (on-command ws (transit/read reader))))

(defn on-bytes [ws bytes offset len]
  (let [buffer (byte-array len)
        voided (System/arraycopy bytes offset buffer 0 len)
        stream (ByteArrayInputStream. buffer)
        reader (transit/reader stream :json)]
    (on-command ws (transit/read reader))))

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
