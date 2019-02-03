(ns illuminepixels.features.games
  (:require [websocket-layer.core :as wl]
            [clojure.core.async :as async]
            [illuminepixels.games.impls :as impls]
            [illuminepixels.games.reactor :as re]
            [illuminepixels.utils :as utils]))

(defonce games (atom {}))

(defn get-game-instance [type slug]
  (letfn [(updater [games k]
            (if (contains? games k)
              games
              (assoc games k (impls/start-game type))))]
    (let [k {:type type :slug slug}]
      (get (swap! games updater k) k))))

(defmethod wl/handle-subscription :game [{:keys [type slug]}]
  (let [game (get-game-instance type slug)
        chan (async/chan)]
    (utils/on-close chan
      (fn [] (re/unsubscribe game chan)))
    (re/subscribe game chan)
    chan))

(defmethod wl/handle-push :mouse-pressed [{:keys [event type slug]}]
  (-> (get-game-instance type slug) (re/mouse-pressed! event)))

(defmethod wl/handle-push :key-pressed [{:keys [event type slug]}]
  (-> (get-game-instance type slug) (re/key-pressed! event)))