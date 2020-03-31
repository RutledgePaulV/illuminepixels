(ns illuminepixels.games.reactor
  (:require [illuminepixels.common :as com]
            [clojure.core.async :as async]
            [clojure.set :as sets]
            [missing.core :as miss]))

(def FPS 30)

(def MAX_LOOP_MILLIS (quot 1000 FPS))


(miss/defonce-protocol GameReactor
  (start [this])
  (subscribe [this chan])
  (unsubscribe [this chan])
  (key-pressed! [this event])
  (mouse-pressed! [this event]))


(defn create-reactor [initial reducer]
  (let [closure (atom {:state initial :subscribers #{} :events []})]
    (letfn [(observe! []
              (add-watch closure :reactor
                (fn [_ _
                     {old-state :state old-subs :subscribers}
                     {new-state :state new-subs :subscribers}]
                  (when (not= old-subs new-subs)
                    (doseq [chan (shuffle (sets/difference new-subs old-subs))]
                      (async/put! chan new-state)))
                  (when (not= old-state new-state)
                    (let [edits (com/get-edits old-state new-state)]
                      (doseq [chan (shuffle (sets/intersection new-subs old-subs))]
                        (async/put! chan edits)))))))
            (game-loop! []
              (async/go-loop []
                (let [updater
                      (fn [{:keys [state events] :as x}]
                        (-> x
                            (assoc :events [])
                            (assoc :state (reducer state events))))
                      [time] (miss/timing (swap! closure updater))]
                  (if (> MAX_LOOP_MILLIS time)
                    (async/<! (async/timeout (- MAX_LOOP_MILLIS time)))
                    (println "Game loop iteration exceeded time allowed by framerate."))
                  (recur))))]
      (reify GameReactor
        (key-pressed! [this event]
          (swap! closure update :events conj {:kind :key :event event}))
        (mouse-pressed! [this event]
          (swap! closure update :events conj {:kind :mouse :event event}))
        (subscribe [this chan]
          (swap! closure update :subscribers conj chan))
        (unsubscribe [this chan]
          (swap! closure update :subscribers disj chan))
        (start [this]
          (observe!) (game-loop!) this)))))

