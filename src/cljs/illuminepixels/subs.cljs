(ns illuminepixels.subs
  (:require
    [re-frame.core :as re-frame]
    [cljs.core.async :as async]
    [reagent.ratom :as ratom]
    [illuminepixels.events :as events]))


(re-frame/reg-sub
  ::name
  (fn [db]
    (:name db)))


(re-frame/reg-sub
  ::active-panel
  (fn [db _]
    (:active-panel db)))

(re-frame/reg-sub
  ::initialised?
  (fn [db _]
    (contains? db :websocket)))

(re-frame/reg-sub-raw
  ::subscribe
  (fn [db [_ query]]
    (let [transaction (random-uuid)
          protocol    :subscription
          values      (deref db)
          sink        (get-in values [:websocket :sink])
          source      (get-in values [:websocket :source])]
      (letfn [(pipe [{:keys [protocol transaction]}]
                (and
                  (= protocol :subscription)
                  (= transaction transaction)))
              (command [datas]
                (merge datas {:transaction transaction :protocol protocol}))]
        (let [sub (async/tap source (async/chan 1 (filter pipe)))]
          (async/go-loop []
            (when-some [event (async/<! sub)]
              (re-frame/dispatch [::events/assoc-in [:subscriptions transaction] (get event :data)])
              (recur)))
          (async/put! sink (command {:data query}))
          (ratom/make-reaction
            (fn [] (get-in @db [:subscriptions transaction]))
            :on-dispose
            (fn []
              (async/close! sub)
              (async/put! sink (command {:unsubscribe true}))
              (re-frame/dispatch [::events/dissoc-in [:subscriptions transaction]]))))))))