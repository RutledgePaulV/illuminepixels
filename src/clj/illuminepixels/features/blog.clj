(ns illuminepixels.features.blog
  (:require [illuminepixels.network.api :as api]
            [hickory.core :as hick]
            [clojure.java.io :as io]
            [illuminepixels.features.introspect :as intro]
            [clojure.string :as string]
            [markdown.core :as md]
            [missing.core :as miss]
            [clojure.string :as strings])
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
    {:metadata (-> (or metadata {})
                   (select-keys [:title])
                   (miss/filter-vals not-empty)
                   (miss/map-vals first))
     :html     (html->hiccup html)}))

(defn get-relative-path [file]
  (let [prefix (str (.getAbsolutePath (io/file "")) "/")]
    (strings/replace (.getAbsolutePath file) prefix "")))

(defn get-blog-posts []
  (let [introspection (intro/summarize-files)]
    (->> (io/resource "posts")
         (io/file)
         (file-seq)
         (filter is-blog-post?)
         (map (juxt (comp introspection get-relative-path) identity))
         (filter (comp some? first))
         (map (fn [[k v]]
                (let [data (markdown->data (slurp v))]
                  (update data :metadata merge k)))))))

(defmethod api/handle-subscribe :blog [data]
  )
