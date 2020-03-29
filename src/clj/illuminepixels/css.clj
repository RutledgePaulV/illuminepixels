(ns illuminepixels.css
  (:require [garden.def :refer [defstyles]]))

(def RAINBOW
  ["#a0d7ff" "#e7c7f9" "#fcc6a3" "#fff4b8" "#c2feb9" "#b8ffff" "#8d8f91" "#c9cbcd"])

(defstyles screen
  [:body {:background-color "rgb(245,245,245)"}]
  [:ul.nav-links [:a {:padding-top "25px"}]]
  [:a.github-corner {:position "fixed" :right "0"}]
  [:.syntax
   [:pre
    {:background    "#282c33"
     :margin-bottom "15px"}
    [[:.definition {:color "#a5f0b4"}]
     [:.core-fn {:color "#a5f0b4"}]
     [:.variable {:color "#e1e1e1"}]
     [:.number {:color "#fcc6a3"}]
     [:.s-exp {}]
     (for [[i color] (map vector (iterate inc 1) RAINBOW)]
       [(keyword (str ".rainbow-" i)) {:color color}])
     [:.symbol {:color "#e1e1e1"}]
     [:.special-form {:color "#a5f0b4"}]
     [:.background {:color "#282c33"}]
     [:.string {:color "#f8ea9e"}]
     [:.keyword {:color "#e7c7f9"}]
     [:.macro {:color "#a5f0b4"}]
     [:.reader-char {:color "#e1e1e1"}]
     [:.nil {:color "#e7c7f9"}]
     [:.comment {:color "#9296a0"}]
     [:.repeat {:color "#a5f0b4"}]
     [:.regex {:color "#f8ea9e"}]
     [:.exception {:color "#a5f0b4"}]
     [:.boolean {:color "#e7c7f9"}]
     [:.character {:color "#f8ea9e"}]
     [:.conditional {:color "#a5f0b4"}]]]])
