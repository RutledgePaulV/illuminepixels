(ns illuminepixels.games.impls
  (:require [illuminepixels.games.reactor :as re]))

(defn rand-r []
  (+ 10 (rand-int 25)))

(defn rand-component []
  (+ 135 (rand-int 120)))

(defn rand-color []
  (vec (take 3 (repeatedly rand-component))))

(defmulti make-game keyword)

(defn start-game [type]
  (re/start (make-game type)))

(defmethod make-game :circles [_]
  (letfn [(reducer [state events]
            (let [added
                  (for [{{:keys [x y]} :event} (reverse events)]
                    {:x x :y y :radius (rand-r) :color (rand-color)})]
              (update state :circles #(vec (take 20 (concat added %))))))]
    (re/new-game {:circles []} reducer)))