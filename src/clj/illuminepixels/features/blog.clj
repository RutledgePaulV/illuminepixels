(ns illuminepixels.features.blog
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [illuminepixels.markdown :as mark]
            [clojure.string :as strings]
            [illuminepixels.utils :as utils]
            [websocket-layer.core :as wl]
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


(defmethod wl/handle-subscription :blogs [data]
  (utils/devpoll 1000 (read-markdowns "posts")))

(defmethod wl/handle-subscription :about [data]
  (utils/devpoll 1000 (first (read-markdowns "about"))))