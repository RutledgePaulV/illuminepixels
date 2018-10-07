(ns illuminepixels.core
  (:require
    [reagent.core :as reagent]
    [re-frame.core :as re-frame]
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
  (let [ready? (re-frame/subscribe [::subs/initialised?])]
    (if-not @ready?
      [:div "Initialising ..."]
      [views/main-panel])))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [top-panel]
    (.getElementById js/document "app")))

(defn initialize []
  (re-frame/dispatch-sync [::events/initialize-db])
  (re-frame/dispatch-sync [::events/websocket-connect {:url "ws://localhost:3000/ws"}]))

(defn ^:export init []
  (routes/app-routes)
  (initialize)
  (dev-setup)
  (mount-root))
