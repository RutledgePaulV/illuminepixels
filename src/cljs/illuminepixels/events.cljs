(ns illuminepixels.events
  (:require
    [re-frame.core :as rf]
    [illuminepixels.db :as db]
    [re-frame-websocket-fx.websocket-fx :as wfx]
    [illuminepixels.utils :as utils]))

(rf/reg-event-fx
  ::initialize
  (fn [_ _]
    {:db         db/default-db
     :dispatch [::wfx/connect :server {:format :transit-json}]}))

(rf/reg-event-db
  ::assoc-in
  (fn [db [_ path value]]
    (assoc-in db path value)))

(rf/reg-event-db
  ::update-in
  (fn [db [_ path f & args]]
    (apply update-in db path f args)))

(rf/reg-event-db
  ::dissoc-in
  (fn [db [_ path]]
    (utils/dissoc-in db path)))

(rf/reg-event-db
  ::route-change
  (fn [db [_ {{name :name} :data
              query-params :query-params
              path-params  :path-params
              template     :template
              path         :path}]]
    (assoc db :active-route
              {:name         name
               :template     template
               :query-params query-params
               :path-params  path-params
               :path         path})))

(rf/reg-event-db
  ::subscription-message
  (fn [db [_ topic initial reducer data]]
    (update-in db [:subscriptions topic] #(reducer (or %1 initial) data))))

(rf/reg-event-fx
  ::subscribe
  (fn [_ [_ query initial reducer]]
    {:dispatch
     [::wfx/subscribe :server query
      {:message    query
       :on-message [::subscription-message query initial reducer]}]}))