(ns illuminepixels.subs
  (:require
    [re-frame.core :as rf]
    [cljs.core.async :as async]
    [reagent.ratom :as ratom]
    [illuminepixels.events :as events]))

(rf/reg-sub ::name
  (fn [db] (:name db)))

(rf/reg-sub ::active-panel
  (fn [db _] (:active-panel db)))

(rf/reg-sub ::initialised?
  (fn [db _] (contains? db :websocket)))

(rf/reg-sub-raw ::subscribe
  (fn [db [_ query initial reducer]]
    (let [new-transaction (random-uuid)
          protocol        :subscription
          values          (deref db)
          sink            (get-in values [:websocket :sink])
          source          (get-in values [:websocket :source])]
      (letfn [(tap [{:keys [protocol transaction]}]
                (and
                  (= protocol :subscription)
                  (= new-transaction transaction)))
              (command [datas]
                (merge datas {:transaction new-transaction :protocol protocol}))]
        (let [sub (async/tap source (async/chan 1 (filter tap)))]
          (async/go-loop []
            (when-some [event (async/<! sub)]
              (rf/dispatch
                [::events/update-in
                 [:subscriptions new-transaction]
                 #((or reducer (fn [_ x] x)) (or % initial) (get event :data))])
              (recur)))
          (async/put! sink (command {:data query}))
          (ratom/make-reaction
            (fn [] (get-in @db [:subscriptions new-transaction] initial))
            :on-dispose
            (fn []
              (async/close! sub)
              (async/put! sink (command {:unsubscribe true}))
              (rf/dispatch [::events/dissoc-in [:subscriptions new-transaction]]))))))))
