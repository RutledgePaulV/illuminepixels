(ns illuminepixels.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:body {:background-color "rgb(245,245,245)"}]
  [:ul.nav-links [:a {:padding-top "25px"}]]
  [:a.github-corner {:position "fixed" :right "0"}])
