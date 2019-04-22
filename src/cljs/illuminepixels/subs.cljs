(ns illuminepixels.subs
  (:require
    [re-frame.core :as rf]
    [reagent.ratom :as ratom]
    [illuminepixels.events :as events]
    [illuminepixels.utils :as utils]
    [websocket-fx.core :as wfx]))

(rf/reg-sub ::db
  (fn [db _] db))

(rf/reg-sub ::active-route
  (fn [db _] (get-in db [:active-route])))

(rf/reg-sub ::initialised?
  :<- [::db]
  :<- [::wfx/status :server]
  (fn [[db status]]
    (and (some? (:active-route db)) (not= :pending status))))

(rf/reg-sub ::disconnected?
  :<- [::wfx/status :server]
  (fn [status]
    (= :reconnecting status)))

(rf/reg-sub ::blogs
  :<- [::subscribe {:kind :blogs}]
  identity)

(rf/reg-sub ::about
  :<- [::subscribe {:kind :about}]
  identity)

(rf/reg-sub ::blog
  :<- [::blogs]
  (fn [blogs [_ slug]]
    (let [key-fn  #(name (get-in % [:metadata :slug]))
          indexed (utils/index-by key-fn blogs)]
      (get indexed (name slug)))))

(rf/reg-sub-raw ::subscribe
  (fn [db [_ query initial reducer]]
    (let [id      (random-uuid)
          reducer (or reducer (fn [_ x] x))]
      (rf/dispatch [::events/subscribe id query initial reducer])
      (ratom/make-reaction
        (fn [] (or (get-in @db [:subscriptions id]) initial))
        :on-dispose
        (fn [] (rf/dispatch [::wfx/unsubscribe :server id]))))))
