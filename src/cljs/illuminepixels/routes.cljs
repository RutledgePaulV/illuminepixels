(ns illuminepixels.routes
  (:require [reitit.core :as r]
            [re-frame.core :as rf]
            [reitit.coercion :as rc]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [reitit.coercion.schema :as rcs]))

(def routes
  [["/" {:name :home-panel}]
   ["/blog"
    ["" {:name :blogs-panel}]
    ["/:slug" {:name :blog-panel}]]
   ["/games"
    ["" {:name :games-panel}]
    ["/:slug" {:name :game-panel}]]
   ["/about" {:name :about-panel}]])

(def router
  (r/router routes
    {:compile rc/compile-request-coercers
     :data    {:coercion rcs/coercion}}))

(defn view->path
  ([view] (view->path view nil))
  ([view path-params]
   (get-in (r/match-by-name router view path-params) [:path])))

(defn restart-router! []
  (let [state (atom nil)]
    (letfn [(apply-controllers [{:keys [controllers]} new-route]
              (->> (rfc/apply-controllers (or controllers []) new-route)
                   (assoc new-route :controllers)))
            (change-route [new-route]
              (let [[o n] (swap-vals! state apply-controllers new-route)]
                (when (not= o n) (rf/dispatch [:illuminepixels.events/route-change n]))))]
      (rfe/start! router change-route {:use-fragment false}))))