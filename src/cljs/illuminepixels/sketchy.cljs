(ns illuminepixels.sketchy
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [re-frame.core :as rf]))

(defn initial-state [command]
  (let [sub (rf/subscribe [:illuminepixels.subs/subscribe command])]
    {:snap @sub :sub sub}))

(defn draw-state [snapshot]
  (q/background 200))

(defn make-sketch
  ([command]
   (make-sketch command [500 500]))
  ([command dimensions]
   (make-sketch command dimensions 30))
  ([command dimensions framerate]
   (letfn [(setup []
             (q/frame-rate framerate)
             (q/color-mode :hsb)
             (initial-state command))

           (draw [{:keys [snap]}]
             (when (some? snap)
               (draw-state snap)))

           (update-state [{:keys [sub] :as state}]
             (update state :snap (constantly @sub)))

           (mouse-press [{:keys [snap] :as state} event]
             (let [message {:kind  :mouse-pressed
                            :game  (get snap :id)
                            :event event}]
               (rf/dispatch [:illuminepixels.events/websocket-message message])
               state))

           (key-press [{:keys [snap] :as state} event]
             (let [message {:kind  :key-pressed
                            :game  (get snap :id)
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
