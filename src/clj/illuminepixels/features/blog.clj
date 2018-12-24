(ns illuminepixels.features.blog
  (:require [hickory.core :as hick]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [markdown.core :as md]
            [missing.core :as miss]
            [clojure.string :as strings]
            [illuminepixels.utils :as utils]
            [illuminepixels.network.api :as api]
            [illuminepixels.introspect :as intro])
  (:import (java.io File)))

(defn is-blog-post? [^File file]
  (and (.isFile file) (string/ends-with? (.getName file) ".md")))

(defn html->hiccup [html]
  (vec (concat [:div] (drop 2 (-> html hick/parse hick/as-hiccup first (nth 3))))))

(defn markdown->data [markdown]
  (let [{:keys [metadata html]}
        (md/md-to-html-string-with-meta markdown
          :heading-anchors true
          :reference-links? true
          :footnotes? true)]
    {:metadata
     (->>
       (or metadata {})
       (miss/filter-vals not-empty)
       (miss/map-vals first))
     :html
     (html->hiccup html)}))

(defn get-relative-path [file]
  (let [prefix (str (.getAbsolutePath (io/file "")) "/")]
    (strings/replace (.getAbsolutePath file) prefix "")))

(defn get-blog-posts []
  (let [introspection (intro/summarize-project)]
    (->> (io/resource "posts")
         (io/file)
         (file-seq)
         (filter is-blog-post?)
         (map (juxt (comp introspection get-relative-path) identity))
         (filter (comp some? first))
         (mapv (fn [[k v]]
                 (let [data (markdown->data (slurp v))]
                   (update data :metadata merge k)))))))

(defmethod api/handle-subscribe :blog [data]
  (utils/polling 1000 (get-blog-posts)))
