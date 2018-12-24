(ns illuminepixels.core
  (:require [illuminepixels.network.socket :as sock]
            [illuminepixels.utils :as utils]
            [illuminepixels.features.blog :as blog]
            [illuminepixels.features.games :as games]
            [mount.core :as mount])
  (:import (org.eclipse.jetty.server Server))
  (:gen-class))


(mount/defstate server
  :start
  (sock/server
    (utils/get-ring-settings))
  :stop
  (when (instance? Server server)
    (.stop server)))


(defn -main [& args]
  (mount/start)
  @(promise))