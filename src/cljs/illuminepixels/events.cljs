(ns illuminepixels.events
  (:require
    [re-frame.core :as rf]
    [illuminepixels.db :as db]
    [websocket-fx.core :as wfx]
    [illuminepixels.utils :as utils]))

(rf/reg-event-fx
  ::initialize
  (fn [_ _]
    {:db
     db/default-db
     :dispatch-n
     [[::wfx/connect
       :server
       {:format     :transit-json
        :on-connect [::wfx/subscribe :server :heartbeat {:message {:kind :ping}}]}]]}))

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
  (fn [_ [_ topic query initial reducer]]
    {:dispatch
     [::wfx/subscribe :server topic
      {:message    query
       :on-message [::subscription-message topic initial reducer]}]}))