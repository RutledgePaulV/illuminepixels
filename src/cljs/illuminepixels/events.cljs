(ns illuminepixels.events
  (:require
    [re-frame.core :as re-frame]
    [illuminepixels.db :as db]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [illuminepixels.utils :as utils]))

(re-frame/reg-event-db
  ::initialize-db
  (fn-traced [_ _]
    db/default-db))

(re-frame/reg-event-db
  ::assoc-in
  (fn-traced [db [_ path value]]
    (assoc-in db path value)))

(re-frame/reg-event-db
  ::dissoc-in
  (fn-traced [db [_ path]]
    (utils/dissoc-in db path)))

(re-frame/reg-event-fx
  ::websocket-connect
  (fn-traced [_ [_ data]]
    {:websocket data}))

(re-frame/reg-event-db
  ::websocket-connected
  (fn-traced [db [_ data]]
    (merge db {:websocket data :requests {} :subscriptions {}})))

(re-frame/reg-event-db
  ::websocket-disconnected
  (fn-traced [db [_ data]]
    (dissoc db :websocket :requests :subscriptions)))