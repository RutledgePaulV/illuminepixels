(ns illuminepixels.features.blog
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [illuminepixels.markdown :as mark]
            [clojure.string :as strings]
            [illuminepixels.utils :as utils]
            [illuminepixels.network.api :as api]
            [illuminepixels.introspect :as intro])
  (:import (java.io File)))

(defn is-markdown? [^File file]
  (and (.isFile file) (string/ends-with? (.getName file) ".md")))

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
                 (let [data (mark/markdown->data (slurp v))]
                   (update data :metadata merge (or k {}))))))))


(defmethod api/handle-subscribe :blogs [data]
  (utils/polling 1000 (read-markdowns "posts")))

(defmethod api/handle-subscribe :about [data]
  (utils/polling 1000 (first (read-markdowns "about"))))