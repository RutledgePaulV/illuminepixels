(ns illuminepixels.utils
  (:require [missing.core :as miss])
  (:import (java.util UUID)))

(def default-ring-settings
  {:port                 3000
   :max-threads          500
   :async?               true
   :allow-null-path-info true
   :send-server-version? false})

(def default-settings
  {:ring default-ring-settings})

(defn get-settings []
  (->> (miss/load-edn-resource "settings.edn")
       (miss/deep-merge default-settings)))

(defn on-close [chan f]
  (add-watch
    (.closed chan)
    (str "close." (UUID/randomUUID))
    (fn [_ _ old-state new-state]
      (when (and (and (not old-state) new-state))
        (miss/quietly (f)))))
  chan)