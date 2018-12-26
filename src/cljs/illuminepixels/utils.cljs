(ns illuminepixels.utils
  (:require [clojure.string :as strings]))


(defn dissoc-in
  [m [k & ks]]
  (if ks
    (if (map? (get m k))
      (update m k #(dissoc-in % ks))
      m)
    (dissoc m k)))

(defn index-by [f coll]
  (into {} (map (juxt f identity) coll)))

(defn deep-merge [& maps]
  (letfn [(inner-merge [& maps]
            (let [ms (remove nil? maps)]
              (if (every? map? ms)
                (apply merge-with inner-merge ms)
                (last ms))))]
    (apply inner-merge maps)))

(defn get-websocket-port []
  (str (aget js/window "location" "port")))

(defn get-websocket-host []
  (str (aget js/window "location" "hostname")))

(defn get-websocket-proto []
  (let [proto (str (aget js/window "location" "protocol"))]
    (get {"http:" "ws" "https:" "wss"} proto)))

(defn websocket-url []
  (let [proto (get-websocket-proto)
        host  (get-websocket-host)
        port  (get-websocket-port)
        path  "/ws"]
    (if (strings/blank? port)
      (str proto "://" host path)
      (str proto "://" host ":" port path))))

(defn eq [& ks]
  (apply = (map name ks)))

(defn parse-date [s]
  (js/Date. (.parse js/Date s)))

(defn format-date [date]
  (cond
    (number? date) (format-date (js/Date date))
    (string? date) (format-date (parse-date date))
    (instance? js/Date date) (.toLocaleDateString date)
    :otherwise (str date)))