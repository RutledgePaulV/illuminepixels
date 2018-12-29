(ns illuminepixels.features.games
  (:require [illuminepixels.network.api :as api]
            [clojure.core.async :as async]
            [illuminepixels.utils :as utils]
            [illuminepixels.common :as com])
  (:import (java.util UUID)))

(defn rand-r [] (+ 10 (rand-int 25)))
(defn rand-component [] (+ 135 (rand-int 120)))
(defn rand-color [] [(rand-component) (rand-component) (rand-component)])

(defonce game-state
  (atom {:circles []}))

(defmethod api/handle-push :mouse-pressed [{{:keys [x y]} :event}]
  (let [state {:x x :y y :color (rand-color) :radius (rand-r)}]
    (swap! game-state update :circles conj state)))

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
          (async/>!! chan (com/get-edits o n)))))
    chan))