(ns illuminepixels.games.proto
  (:require [clojure.core.async :as async]
            [illuminepixels.common :as com]
            [clojure.set :as sets]
            [missing.core :as miss]))



(def FREQ (quot 1000 30))
(defn rand-r [] (+ 10 (rand-int 25)))
(defn rand-component [] (+ 135 (rand-int 120)))
(defn rand-color [] [(rand-component) (rand-component) (rand-component)])

(defprotocol Game
  (subscribe [this chan])
  (unsubscribe [this chan])
  (key-pressed! [this event])
  (mouse-pressed! [this event])
  (update! [this f]))

(defn new-game [initial on-mouse on-keyboard]
  (let [closure     (atom initial)
        subscribers (atom #{})]
    (add-watch closure :reactor
      (fn [k r o n]
        (when (not= o n)
          (let [diff (com/get-edits o n)]
            (doseq [chan (shuffle @subscribers)]
              (async/put! chan diff))))))
    (add-watch subscribers :reactor
      (fn [k r o n]
        (let [state @closure]
          (doseq [chan (sets/difference n o)]
            (async/put! chan state)))))
    (reify Game
      (update! [this f]
        (swap! closure f))
      (mouse-pressed! [this event]
        (on-mouse event))
      (key-pressed! [this event]
        (on-keyboard event))
      (subscribe [this chan]
        (swap! subscribers conj chan))
      (unsubscribe [this chan]
        (swap! closure disj chan)))))


(defn default-game [state]
  (let [event-queue      (atom [])
        mouse-handler    (fn [event]
                           (let [data {:kind :mouse :event event}]
                             (swap! event-queue conj data)))
        keyboard-handler (fn [event]
                           (let [data {:kind :keyboard :event event}]
                             (swap! event-queue conj data)))]
    [event-queue (new-game state mouse-handler keyboard-handler)]))

(defn start-loop [init f]
  (let [[queue game] (default-game init)]
    (async/go-loop []
      (let [[events] (reset-vals! queue [])
            [time] (miss/timing (update! game #(f % events)))]
        (println time)
        (when (> FREQ time)
          (async/<! (async/timeout (- FREQ time))))
        (recur)))
    game))

(defmulti start-game keyword)

(defmethod start-game :circles [_]
  (letfn [(reducer [state events]
            (->> (for [{{:keys [x y]} :event} events]
                   {:x x :y y :radius (rand-r) :color (rand-color)})
                 (update state :circles (comp vec concat))))]
    (start-loop {:circles []} reducer)))