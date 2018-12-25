(ns illuminepixels.features.games
  (:require [illuminepixels.games.snake :as snake]
            [illuminepixels.network.api :as api]
            [illuminepixels.utils :as utils]
            [clojure.core.async :as async]))

(defn games []
  [{:name        "Snake"
    :slug        :snake
    :description "Chase each other around in real time."}])


(defmethod api/handle-subscribe :games [data]
  (let [chan (async/chan 1)]
    (async/>!! chan (games))
    chan))