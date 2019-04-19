(ns user
  (:require [figwheel-sidecar.repl-api :as repl]
            [illuminepixels.core :as core]
            [mount.core :as mount]))


(defn restart-server []
  (mount/stop)
  (mount/start))

(defn start-frontend-repl []
  (repl/start-figwheel!)
  (repl/cljs-repl))