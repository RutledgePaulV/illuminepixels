(ns illuminepixels.introspect
  (:require [clj-jgit.porcelain :as jgit]
            [missing.core :as miss])
  (:import (org.eclipse.jgit.revwalk RevCommit)
           (org.eclipse.jgit.lib Constants PersonIdent)
           (org.eclipse.jgit.treewalk TreeWalk)
           (org.eclipse.jgit.api Git)))

(defn person->data [^PersonIdent person]
  {:name  (.getName person)
   :email (.getEmailAddress person)})

(defn commit->data [^Git repo ^RevCommit commit]
  {:commit    (.name commit)
   :type      (keyword (Constants/typeString (.getType commit)))
   :timestamp (.getCommitTime commit)
   :message   (.getFullMessage commit)
   :author    (person->data (.getAuthorIdent commit))
   :committer (person->data (.getCommitterIdent commit))
   :files     (let [walk (doto (TreeWalk. (.getRepository repo))
                           (.setRecursive true))]
                (.reset walk (.getId (.getTree commit)))
                (loop [results #{}]
                  (if (.next walk)
                    (recur (conj results (.getPathString walk)))
                    results)))})

(defn summarize-commits [commits]
  {:created  (select-keys (first commits) [:commit :timestamp :message :author])
   :modified (select-keys (last commits) [:commit :timestamp :message :author])})

(defn summarize-project []
  (let [repo (jgit/load-repo ".")]
    (->>
      (jgit/git-log repo)
      (map (partial commit->data repo))
      (mapcat (fn [commit]
                (let [without (dissoc commit :files)]
                  (map #(assoc without :file %) (:files commit)))))
      (sort-by :timestamp)
      (group-by :file)
      (miss/map-vals summarize-commits))))