(ns illuminepixels.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:ul.nav-links [:a {:padding-top "25px"}]]
  [:a.github-corner {:position "fixed" :right "0"}])
