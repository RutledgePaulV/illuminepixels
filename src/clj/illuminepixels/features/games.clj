(ns illuminepixels.features.games
  (:require [illuminepixels.network.api :as api]
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

(defmethod api/handle-subscribe :game [{:keys [type slug]}]
  (let [game (get-game-instance type slug)
        chan (async/chan)]
    (utils/on-close chan
      (fn [] (re/unsubscribe game chan)))
    (re/subscribe game chan)
    chan))

(defmethod api/handle-push :mouse-pressed [{:keys [event type slug]}]
  (-> (get-game-instance type slug) (re/mouse-pressed! event)))

(defmethod api/handle-push :key-pressed [{:keys [event type slug]}]
  (-> (get-game-instance type slug) (re/key-pressed! event)))