(ns illuminepixels.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.History)
  (:require
    [secretary.core :as secretary]
    [goog.events :as gevents]
    [goog.history.EventType :as EventType]
    [re-frame.core :as rf]
    [illuminepixels.events :as events]))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/reset-routes!)

  (secretary/set-config! :prefix "#")
  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (rf/dispatch [::events/assoc-in [:active-panel] :home-panel]))

  (defroute "/about" []
    (rf/dispatch [::events/assoc-in [:active-panel] :about-panel]))

  (defroute "/blog" []
    (rf/dispatch [::events/assoc-in [:active-panel] :blog-panel]))

  (defroute "/games" []
    (rf/dispatch [::events/assoc-in [:active-panel] :game-panel]))

  ;; --------------------
  (hook-browser-navigation!))
