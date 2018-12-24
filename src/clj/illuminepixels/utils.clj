(ns illuminepixels.utils
  (:require [missing.core :as miss]
            [clojure.core.async :as async])
  (:import (java.util UUID)))

(def default-settings
  {:ring
   {:port                 3000
    :max-threads          500
    :async?               true
    :allow-null-path-info true
    :send-server-version? false}})

(defn get-settings []
  (->> (miss/load-edn-resource "settings.edn")
       (miss/deep-merge default-settings)))

(defn get-ring-settings []
  (get (get-settings) :ring))

(defn on-close [chan f]
  (add-watch
    (.closed chan)
    (str "close." (UUID/randomUUID))
    (fn [_ _ old-state new-state]
      (when (and (and (not old-state) new-state))
        (miss/quietly (f)))))
  chan)

(defmacro polling [timeout & body]
  `(let [chan# (async/chan)
         func# (fn [] ~@body)
         freq# ~timeout]
     (async/go-loop [prev# ::impossible]
       (let [result# (func#)]
         (if (not= result# prev#)
           (when (async/>! chan# result#)
             (async/<! (async/timeout freq#))
             (recur result#))
           (do (async/<! (async/timeout freq#))
               (recur result#)))))
     chan#))

(defmacro polling-blocking [timeout & body]
  `(let [chan# (async/chan)
         func# (fn [] ~@body)
         freq# ~timeout]
     (async/go-loop [prev# ::impossible]
       (let [result# (async/<! (async/thread (func#)))]
         (if (not= result# prev#)
           (when (async/>! chan# result#)
             (async/<! (async/timeout freq#))
             (recur result#))
           (do (async/<! (async/timeout freq#))
               (recur result#)))))
     chan#))