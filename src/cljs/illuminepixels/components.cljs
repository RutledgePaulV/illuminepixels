(ns illuminepixels.components
  (:require [quil.core :as q]))



(defn canvas [id]
  [:canvas {:id id :style {:height "100%" :width "100%"}}])

(defn sketch [sketch]
  [(with-meta
     canvas
     {:component-did-mount
      (fn [component] (sketch))
      :component-will-unmount
      (fn [component])})
   (-> sketch meta :name)])