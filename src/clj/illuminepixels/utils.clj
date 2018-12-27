(ns illuminepixels.utils
  (:require [missing.core :as miss]
            [clojure.core.async :as async]
            [clojure.walk :as walk])
  (:import (java.util UUID)
           (clojure.lang ExceptionInfo)))

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

(defn search [pred form]
  (try
    (walk/postwalk
      (fn [form]
        (if (pred form)
          (throw (ex-info "" {::stone form}))
          form))
      form)
    nil
    (catch ExceptionInfo e
      (if-some [[_ value] (find (ex-data e) ::stone)]
        value
        (throw e)))))

(defmacro once [& body]
  `(let [chan# (async/chan)]
     (async/put! chan# (do ~@body))
     chan#))

(defmacro polling [timeout & body]
  `(let [chan# (async/chan)
         shut# (async/promise-chan)
         func# (fn [] ~@body)
         freq# ~timeout]
     (on-close chan# (fn [] (async/put! shut# ::close)))
     (async/go-loop [prev# ::impossible]
       (let [result# (async/<! (async/thread (func#)))]
         (when (and (some? result#) (not= result# prev#))
           (async/>! chan# result#))
         (async/<! (async/timeout freq#))
         (when-not (= ::close (async/poll! shut#))
           (recur result#))))
     chan#))