(ns illuminepixels.sketchy
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [re-frame.core :as rf]
            [illuminepixels.common :as com]))

(defn initial-state [type slug]
  (let [reducer #(if (nil? %1) %2 (com/apply-edits %1 %2))
        message [:illuminepixels.subs/subscribe
                 {:kind :game :type type :slug slug}
                 nil
                 reducer]
        sub     (rf/subscribe message)]
    {:snap @sub :sub sub :type type :slug slug}))

(defn draw-state [{:keys [circles]}]
  (q/background 200)
  (doseq [{:keys [x y radius color]} circles]
    (apply q/fill color)
    (q/ellipse x y radius radius)))

(defn make-sketch
  ([type slug]
   (make-sketch type slug [500 500]))
  ([type slug dimensions]
   (make-sketch type slug dimensions 30))
  ([type slug dimensions framerate]
   (letfn [(setup []
             (q/frame-rate framerate)
             (q/color-mode :hsb)
             (initial-state type slug))

           (draw [{:keys [snap]}]
             (when (some? snap)
               (draw-state snap)))

           (update-state [{:keys [sub] :as state}]
             (update state :snap (constantly @sub)))

           (mouse-press [{:keys [type slug] :as state} event]
             (let [message {:kind  :mouse-pressed
                            :type  type
                            :slug  slug
                            :event event}]
               (rf/dispatch [:illuminepixels.events/websocket-message message])
               state))

           (key-press [{:keys [type slug] :as state} event]
             (let [message {:kind  :key-pressed
                            :type  type
                            :slug  slug
                            :event event}]
               (rf/dispatch [:illuminepixels.events/websocket-message message])
               state))]

     (let [host (str (random-uuid))]
       (with-meta
         (fn []
           (apply q/sketch
                  (mapcat identity
                    {:host          host
                     :size          dimensions
                     :setup         setup
                     :update        update-state
                     :draw          draw
                     :mouse-pressed mouse-press
                     :key-typed     key-press
                     :middleware    [m/fun-mode]})))
         {:name host :export true})))))
