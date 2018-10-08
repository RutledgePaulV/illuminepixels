(ns illuminepixels.core
  (:require [illuminepixels.network.handlers :as handlers]
            [illuminepixels.utils :as utils]
            [ring.adapter.jetty9 :as jetty])
  (:gen-class))




(defn -main [& args]
  (let [settings   (:ring (utils/get-settings))
        ws-options {:websockets {"/ws" handlers/websocket-handler}}]
    (jetty/run-jetty handlers/web-handler (merge settings ws-options))))
