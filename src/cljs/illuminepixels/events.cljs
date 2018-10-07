(ns illuminepixels.events
  (:require
    [re-frame.core :as rf]
    [illuminepixels.db :as db]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [illuminepixels.utils :as utils]))

(rf/reg-event-db
  ::initialize-db
  (fn-traced [_ _]
    db/default-db))

(rf/reg-event-db
  ::assoc-in
  (fn-traced [db [_ path value]]
    (assoc-in db path value)))

(rf/reg-event-db
  ::update-in
  (fn-traced [db [_ path f & args]]
    (apply update-in db path f args)))

(rf/reg-event-db
  ::dissoc-in
  (fn-traced [db [_ path]]
    (utils/dissoc-in db path)))

(rf/reg-event-fx
  ::websocket-connect
  (fn-traced [_ [_ data]]
    {:websocket data}))

(rf/reg-event-db
  ::websocket-connected
  (fn-traced [db [_ data]]
    (merge db {:websocket data :requests {} :subscriptions {}})))

(rf/reg-event-db
  ::websocket-disconnected
  (fn-traced [db [_ data]]
    (dissoc db :websocket :requests :subscriptions)))