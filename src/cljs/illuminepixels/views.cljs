(ns illuminepixels.views
  (:require
    [re-frame.core :as rf]
    [illuminepixels.subs :as subs]))


;; home


(defn navigation [panel]
  [:nav
   [:div.nav-container
    [:div.nav-logo
     [:a {:href "#/"} "home"]]
    [:ul.nav-links
     [:li (if (= :blog-panel panel)
            [:a.active {:href "#/blog"} [:h6 "blog"]]
            [:a {:href "#/blog"} [:h6 "blog"]])]
     [:li (if (= :game-panel panel)
            [:a.active {:href "#/games"} [:h6 "games"]]
            [:a {:href "#/games"} [:h6 "games"]])]
     [:li (if (= :about-panel panel)
            [:a.active {:href "#/about"} [:h6 "about"]]
            [:a {:href "#/about"} [:h6 "about"]])]]]])

(defn home-panel []
  )

(defn blog-panel []
  (let [blogs (rf/subscribe [::subs/subscribe {:kind :blog}])]
    (for [entry @blogs]
      [:div {:key (str (random-uuid))}
       [:h2 (get-in entry [:metadata :title])]
       [:h3 (get-in entry [:metadata :created :author :name])]
       (:html entry)])))

(defn game-panel []
  [:div [:h1 "This is the games page."]])

(defn about-panel []
  [:div [:h1 "This is the games page."]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :game-panel [game-panel]
    :blog-panel [blog-panel]
    :about-panel [about-panel]
    [:div "Not found!"]))

(defn show-panel [panel-name]
  [:div [navigation panel-name]
   [panels panel-name]])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
