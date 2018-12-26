(ns illuminepixels.network.api
  (:require [clojure.core.async :as async])
  (:import (java.util UUID)))

(def ^:dynamic *state* nil)

(defn new-state [request]
  {:id            (UUID/randomUUID)
   :messages      (async/chan 20)
   :subscriptions {}
   :state         {}
   :request       request})

(defn get-subscriptions []
  (some-> *state* deref :subscriptions))

(defn get-messages []
  (some-> *state* deref :messages))

(defn get-state []
  (some-> *state* deref :state))

(defn get-request []
  (some-> *state* deref :request))

(def dispatch (comp keyword :kind))
(defmulti handle-push dispatch)
(defmulti handle-request dispatch)
(defmulti handle-subscribe dispatch)


(defmethod handle-subscribe :ping [{:keys [millis] :or {millis 10000}}]
  (let [response (async/chan)]
    (async/go-loop [counter 0]
      (when (async/>! response {:pong true :count counter})
        (async/<! (async/timeout millis))
        (recur (inc counter))))
    response))