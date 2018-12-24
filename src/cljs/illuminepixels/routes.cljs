(ns illuminepixels.routes
  (:import goog.History)
  (:require
    [reitit.core :as r]
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
   ["/about" {:name :about-panel}]
   ["/*other" {:name :not-found}]])

(def router-options
  {:compile   rc/compile-request-coercers
   :data      {:coercion rcs/coercion}
   :conflicts nil})

(def router (r/router routes router-options))

(defn view->path
  ([view]
   (-> (r/match-by-name router view) (get-in [:path])))
  ([view path-params]
   (-> (r/match-by-name router view path-params)
       (get-in [:path]))))

(def most-recent-route (atom nil))

(add-watch most-recent-route :dispatcher
  (fn [k r o n] (when (not= o n) (rf/dispatch [:illuminepixels.events/route-change n]))))

(defn on-navigation [new-route]
  (swap! most-recent-route
         (fn [{:keys [controllers]}]
           (when new-route
             (->> (rfc/apply-controllers (or controllers []) new-route)
                  (assoc new-route :controllers))))))

(defn app-routes []
  (rfe/start! router on-navigation {:use-fragment false}))