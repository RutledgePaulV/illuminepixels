(ns illuminepixels.utils
  (:require [missing.core :as miss]
            [clojure.core.async :as async]
            [websocket-layer.core :as wl]))

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

(defmacro once [& body]
  `(let [chan# (async/chan)]
     (async/put! chan# (do ~@body))
     chan#))

(defn on-chan-close [chan callback]
  (let [state (.closed chan)
        ident (miss/uuid)]
    (add-watch state ident
               (fn [k r o n]
                 (when (and (not o) n)
                   (remove-watch r ident)
                   (miss/quietly (callback)))))))

(defmacro polling [timeout & body]
  `(let [chan# (async/chan)
         shut# (async/promise-chan)
         func# (fn [] ~@body)
         freq# ~timeout]
     (on-chan-close chan# (fn [] (async/put! shut# ::close)))
     (async/go-loop [prev# ::impossible]
       (let [result# (async/<! (async/thread (func#)))]
         (when (and (some? result#) (not= result# prev#))
           (async/>! chan# result#))
         (async/<! (async/timeout freq#))
         (when-not (= ::close (async/poll! shut#))
           (recur result#))))
     chan#))