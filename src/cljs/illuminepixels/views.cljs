(ns illuminepixels.views
  (:require
    [re-frame.core :as rf]
    [illuminepixels.subs :as subs]))


;; home

(defn navigation []
  [:nav
   [:div.nav-container
    [:div.nav-logo
     [:a.active {:href "#/"} "home"]]
    [:ul.nav-links
     [:li
      [:a {:href "#/games"} [:h6 "games"]]]
     [:li
      [:a {:href "#/blog"} [:h6 "blog"]]]
     [:li
      [:a {:href "#/about"} [:h6 "about"]]]]]])

(defn home-panel []
  [:div
   [navigation]])


;; about

(defn about-panel []
  [:div
   [:h1 "This is the About Page."]

   [:div
    [:a {:href "#/"}
     "go to Home Page"]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
