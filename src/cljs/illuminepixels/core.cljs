(ns illuminepixels.core
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as rf]
    [illuminepixels.events :as events]
    [illuminepixels.routes :as routes]
    [illuminepixels.views :as views]
    [illuminepixels.config :as config]
    [illuminepixels.subs :as subs]
    [illuminepixels.handlers :as handlers]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn top-panel []
  (let [ready? (rf/subscribe [::subs/initialised?])]
    (if-not @ready?
      [:div "Initialising ..."]
      [views/main-panel])))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [top-panel]
    (.getElementById js/document "app")))

(defn initialize []
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/websocket-connect {:url "ws://localhost:3000/ws"}]))

(defn ^:export init []
  (routes/app-routes)
  (initialize)
  (dev-setup)
  (mount-root))
