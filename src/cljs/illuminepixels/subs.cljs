(ns illuminepixels.subs
  (:require
    [re-frame.core :as rf]
    [cljs.core.async :as async]
    [reagent.ratom :as ratom]
    [illuminepixels.events :as events]
    [illuminepixels.utils :as utils]))

(rf/reg-sub ::active-route
  (fn [db _] (get-in db [:active-route])))

(rf/reg-sub ::initialised?
  (fn [db _] (and (contains? db :websocket) (contains? db :active-route))))

(rf/reg-sub ::blogs :<- [::subscribe {:kind :blogs}] identity)
(rf/reg-sub ::games :<- [::subscribe {:kind :games}] identity)
(rf/reg-sub ::about :<- [::subscribe {:kind :about}] identity)

(rf/reg-sub ::blog
  :<- [::blogs]
  (fn [blogs [_ slug]]
    (let [key-fn  #(name (get-in % [:metadata :slug]))
          indexed (utils/index-by key-fn blogs)]
      (get indexed (name slug)))))

(rf/reg-sub ::game
  :<- [::games]
  (fn [games [_ slug]]
    (let [key-fn  #(name (get-in % [:slug]))
          indexed (utils/index-by key-fn games)]
      (get indexed (name slug)))))

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
