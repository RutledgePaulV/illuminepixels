(ns illuminepixels.core
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [illuminepixels.events :as events]
    [illuminepixels.routes :as routes]
    [illuminepixels.views :as views]
    [illuminepixels.subs :as subs]))

(defn top-panel []
  (let [ready? (rf/subscribe [::subs/initialised?])]
    (if-not @ready?
      [:div "Initialising ..."]
      [views/main-panel])))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [top-panel]
    (.getElementById js/document "app")))

(defn initial-events []
  (rf/dispatch-sync [::events/initialize]))

(defn ^:export init []
  (enable-console-print!)
  (routes/restart-router!)
  (initial-events)
  (mount-root))
