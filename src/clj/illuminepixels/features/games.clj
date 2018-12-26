(ns illuminepixels.features.games
  (:require [illuminepixels.network.api :as api]
            [illuminepixels.utils :as utils]))

(defn games []
  [{:name        "Othello"
    :slug        :othello
    :description "Play others at the game othello."}])


(defmethod api/handle-subscribe :games [data]
  (utils/once (games)))