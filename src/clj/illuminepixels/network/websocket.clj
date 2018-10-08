(ns illuminepixels.network.websocket
  (:require [clojure.core.async :as async]))

(def ^:dynamic *state* nil)
(def dispatch (comp keyword :kind))
(defmulti handle-push dispatch)
(defmulti handle-request dispatch)
(defmulti handle-subscribe dispatch)


(defmethod handle-subscribe :ping [{:keys [millis] :or {millis 5000}}]
  (let [response (async/chan)]
    (async/go-loop [counter 0]
      (when (async/>! response {:pong true :count counter})
        (async/<! (async/timeout millis))
        (recur (inc counter))))
    response))