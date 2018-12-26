(ns illuminepixels.core
  (:require [illuminepixels.network.socket :as sock]
            [illuminepixels.utils :as utils]
            [illuminepixels.features.peers :as peers]
            [illuminepixels.features.games :as games]
            [illuminepixels.features.blog :as blog]
            [mount.core :as mount])
  (:import (org.eclipse.jetty.server Server))
  (:gen-class))


(mount/defstate ^{:on-reload :noop} server
  :start
  (sock/server
    (utils/get-ring-settings))
  :stop
  (when (instance? Server server)
    (.stop server)))


(defn -main [& args]
  (mount/start)
  @(promise))