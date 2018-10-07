(ns illuminepixels.handlers.handlers
  (:require [compojure.core :refer :all]
            [compojure.route :refer [resources]]
            [ring.adapter.jetty9.websocket :as jws]
            [illuminepixels.handlers.web :as web]
            [illuminepixels.handlers.websocket :as ws]
            [ring.util.response :refer [resource-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [clojure.edn :as edn]
            [clojure.core.async :as async]
            [missing.core :as miss]
            [illuminepixels.utils :as utils]
            [ring.util.response :as response]))


(defroutes web-routes
  (GET "/" []
    (resource-response "index.html" {:root "public"}))

  (resources "/")

  (ANY "/**" [] (response/not-found "Not found.")))

(defn on-bytes [& args]
  (throw (ex-info "Unsupported websocket method." {})))

(def ^:dynamic *subscriptions* nil)
(def ^:dynamic *messages* nil)

(def REQUEST_PROTO :request)
(def SUBSCRIPTION_PROTO :subscription)
(def PUSH_PROTO :push)

(defn callbacks-for-chan [chan]
  {:write-failed  (fn [e] (async/close! chan))
   :write-success (fn [] (async/close! chan))})

(defn send-message! [ws data]
  (let [finished (async/promise-chan)]
    (jws/send! ws (pr-str data) (callbacks-for-chan finished))
    finished))

(defn on-connect [ws]
  (let [out *messages*]
    (async/go-loop []
      (when-some [msg (async/<! out)]
        (async/<! (send-message! ws msg))
        (recur)))))

(defn on-error [ws e]
  (.printStackTrace e))

(defn on-close [ws status reason]
  (doseq [sub (vals @*subscriptions*)]
    (miss/quietly (async/close! sub)))
  (miss/quietly (async/close! *messages*)))

(defn on-text [ws message]
  (let [command     (edn/read-string message)
        protocol    (get command :protocol)
        transaction (get command :transaction)]
    (cond
      (#{REQUEST_PROTO} protocol)
      (let [response (ws/handle-request (:data command))]
        (async/put! *messages* {:data response :protocol REQUEST_PROTO :transaction transaction}))
      (#{SUBSCRIPTION_PROTO} protocol)
      (let [subs (deref *subscriptions*)]
        (if (contains? subs transaction)
          (when (get command :unsubscribe)
            (let [sub (get subs transaction)]
              (async/close! sub)))
          (let [response (ws/handle-subscribe (:data command))]
            (utils/on-close response (fn [] (swap! *subscriptions* dissoc transaction)))
            (swap! *subscriptions* assoc transaction response)
            (async/go-loop []
              (when-some [res (async/<! response)]
                (if (async/>! *messages* {:data res :protocol SUBSCRIPTION_PROTO :transaction transaction})
                  (recur)
                  (async/close! response)))))))
      (#{PUSH_PROTO} protocol)
      (ws/handle-push command))))

(defn websocket-routes [req]
  (let [state (atom {})
        subs  (atom {})
        chan  (async/chan 20)]
    (letfn [(mw [handler]
              (fn [& args]
                (binding [*subscriptions* subs
                          *messages*      chan
                          ws/*state*      state]
                  (apply handler args))))]
      (->> {:on-connect on-connect
            :on-error   on-error
            :on-close   on-close
            :on-text    on-text
            :on-bytes   on-bytes}
           (miss/map-vals mw)))))

(defn wrap-web-middleware [handler]
  (-> handler wrap-reload))

(defn wrap-websocket-middleware [handler]
  (-> handler wrap-reload))

(def web-handler
  (-> #'web-routes wrap-web-middleware))

(def websocket-handler
  (-> #'websocket-routes wrap-websocket-middleware))