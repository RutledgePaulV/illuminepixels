(ns illuminepixels.features.games
  (:require [illuminepixels.network.api :as api]
            [clojure.core.async :as async]
            [illuminepixels.utils :as utils])
  (:import (java.util UUID)))


(defonce game-state
  (atom {:id (UUID/randomUUID) :circles []}))

(defmethod api/handle-push :mouse-pressed [{{:keys [x y]} :event}]
  (swap! game-state update :circles conj [:x x :y y]))

(defmethod api/handle-push :key-pressed [data]
  (println "received key press event:" data)
  (swap! game-state update :circles conj [:x 10 :y 10]))

(defmethod api/handle-subscribe :circles [data]
  (let [chan  (async/chan)
        watch (UUID/randomUUID)]
    (async/put! chan @game-state)
    (utils/on-close chan
      (fn [] (remove-watch game-state watch)))
    (add-watch game-state watch
      (fn [k r o n]
        (when (not= o n)
          (async/>!! chan n))))
    chan))