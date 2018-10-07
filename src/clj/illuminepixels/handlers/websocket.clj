(ns illuminepixels.handlers.websocket
  (:require [clojure.core.async :as async]))

(def ^:dynamic *state* nil)
(def dispatch (comp keyword :kind))
(defmulti handle-push dispatch)
(defmulti handle-request dispatch)
(defmulti handle-subscribe dispatch)



(defmethod handle-subscribe :ping [command]
  (let [counter  (atom 0)
        response (async/chan)]
    (async/go-loop []
      (async/<! (async/timeout 5000))
      (async/>! response {:pong true :count (swap! counter inc)})
      (recur))
    response))