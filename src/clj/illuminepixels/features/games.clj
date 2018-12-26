(ns illuminepixels.features.games
  (:require [illuminepixels.network.api :as api]
            [illuminepixels.utils :as utils]
            [clojure.core.async :as async]))

(defn games []
  [{:name        "Othello"
    :slug        :othello
    :description "Play others at the game othello."}])


(defmethod api/handle-subscribe :games [data]
  (let [chan (async/chan 1)]
    (async/>!! chan (games))
    chan))