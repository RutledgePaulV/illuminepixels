(ns illuminepixels.features.games
  (:require [illuminepixels.network.api :as api]
            [clojure.core.async :as async]
            [illuminepixels.games.proto :as pro]
            [illuminepixels.utils :as utils]))

(defonce games (atom {}))

(defn get-game-instance [type slug]
  (letfn [(updater [games k]
            (if (contains? games k)
              games
              (assoc games k (pro/start-game type))))]
    (let [k {:type type :slug slug}]
      (get (swap! games updater k) k))))

(defmethod api/handle-subscribe :game [{:keys [type slug]}]
  (let [game (get-game-instance type slug)
        chan (async/chan)]
    (utils/on-close chan
      (fn [] (pro/unsubscribe game chan)))
    (pro/subscribe game chan)
    chan))

(defmethod api/handle-push :mouse-pressed [{:keys [event type slug]}]
  (let [game (get-game-instance type slug)]
    (pro/mouse-pressed! game event)))

(defmethod api/handle-push :key-pressed [{:keys [event type slug]}]
  (let [game (get-game-instance type slug)]
    (pro/key-pressed! game event)))