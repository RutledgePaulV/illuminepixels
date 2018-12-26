(ns illuminepixels.features.blog
  (:require [hickory.core :as hick]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [markdown.core :as md]
            [missing.core :as miss]
            [clojure.string :as strings]
            [illuminepixels.utils :as utils]
            [illuminepixels.network.api :as api]
            [illuminepixels.introspect :as intro]
            [glow.core :as glow]
            [clojure.core.async :as async]
            [clojure.walk :as walk])
  (:import (java.io File)
           (clojure.lang ExceptionInfo)))

(defn is-markdown? [^File file]
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

(defn read-markdowns [dir]
  (let [introspection (intro/summarize-project)]
    (->> (io/resource dir)
         (io/file)
         (file-seq)
         (filter is-markdown?)
         (map (juxt (comp introspection get-relative-path) identity))
         (mapv (fn [[k v]]
                 (let [data (markdown->data (slurp v))]
                   (update data :metadata merge (or k {}))))))))

(defmethod api/handle-subscribe :highlight [{:keys [html]}]
  (letfn [(extract [node]
            (try
              (walk/postwalk
                (fn [form]
                  (if (and (vector? form) (= :code (first form)))
                    (let [code (if (map? (second form))
                                 (nth form 2)
                                 (second form))]
                      (throw (ex-info "" {::code code})))
                    form)) node)
              (catch ExceptionInfo found
                (::code (ex-data found)))))]
    (let [scheme      (miss/load-edn-resource "schemes/borealis.edn")
          as-string   (extract html)
          highlighted (glow/highlight-html as-string)
          as-hiccup   (html->hiccup highlighted)
          css         (glow/generate-css scheme)]
      (let [chan (async/chan 1)]
        (async/>!! chan {:html as-hiccup :css css})
        chan))))

(defmethod api/handle-subscribe :blogs [data]
  (utils/polling 1000 (read-markdowns "posts")))

(defmethod api/handle-subscribe :about [data]
  (utils/polling 1000 (first (read-markdowns "about"))))