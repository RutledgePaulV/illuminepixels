(ns illuminepixels.subs
  (:require
    [re-frame.core :as rf]
    [reagent.ratom :as ratom]
    [illuminepixels.events :as events]
    [illuminepixels.utils :as utils]
    [re-frame-websocket-fx.websocket-fx :as wfx]))

(rf/reg-sub ::db
  (fn [db _] db))

(rf/reg-sub ::active-route
  (fn [db _] (get-in db [:active-route])))

(rf/reg-sub ::initialised?
  :<- [::db]
  :<- [::wfx/status :server]
  (fn [[db status]]
    (and (some? (:active-route db)) (= :connected status))))

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
  (fn [db [_ topic initial reducer]]
    (let [reducer (or reducer (fn [_ x] x))]
      (rf/dispatch [::events/subscribe topic initial reducer])
      (ratom/make-reaction
        (fn [] (or (get-in @db [:subscriptions topic]) initial))
        :on-dispose
        (fn [] (rf/dispatch [:re-frame-websocket-fx.websocket-fx/cancel-subscription :server topic]))))))
