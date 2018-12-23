(ns illuminepixels.network.socket
  (:require [illuminepixels.network.http :as http]
            [illuminepixels.network.websocket :as ws]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.adapter.jetty9 :as jetty]
            [missing.core :as miss]))


(defn wrap-web-middleware [handler]
  (-> handler wrap-reload))

(defn wrap-websocket-middleware [handler]
  (-> handler wrap-reload))

(def web-handler
  (wrap-web-middleware #'http/web-routes))

(def websocket-handler
  (wrap-websocket-middleware #'ws/websocket-routes))

(defn server [settings]
  (let [forced-options {:join? false :websockets {"/ws" websocket-handler}}]
    (jetty/run-jetty web-handler (miss/deep-merge settings forced-options))))