(ns illuminepixels.common
  (:require [editscript.core :as es]
            [editscript.edit :as ed]))


(defn get-edits [a b]
  (ed/get-edits (es/diff a b)))

(defn apply-edits [a edits]
  (let [script (ed/->EditScript edits true 0 0 0 0)]
    (es/patch a script)))
