(ns illuminepixels.core
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [illuminepixels.events :as events]
    [illuminepixels.routes :as routes]
    [illuminepixels.views :as views]
    [illuminepixels.subs :as subs]
    [illuminepixels.utils :as utils]
    [illuminepixels.cofx :as cofx]))

(defn top-panel []
  (let [ready? (rf/subscribe [::subs/initialised?])
        route  (rf/subscribe [::subs/active-route])]
    (if-not @ready?
      [:div "Initialising ..."]
      [views/main-panel @route])))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [top-panel]
    (.getElementById js/document "app")))

(defn initial-events []
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/websocket-connect {:url (utils/websocket-url)}]))

(defn ^:export init []
  (enable-console-print!)
  (routes/restart-router!)
  (initial-events)
  (mount-root))
