(ns illuminepixels.network.socket
  (:require [illuminepixels.network.http :as http]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.adapter.jetty9 :as jetty]
            [taoensso.timbre :as logger]
            [websocket-layer.network :as net]
            [missing.core :as miss]))


(defn wrap-web-middleware [handler]
  (-> handler wrap-reload))

(defn wrap-websocket-middleware [handler]
  (-> handler wrap-reload))

(def web-handler
  (wrap-web-middleware #'http/web-routes))

(def websocket-handler
  (wrap-websocket-middleware
    (net/websocket-handler
      {:exception-handler #(logger/error %)
       :encoding          :transit-json})))

(defn server [settings]
  (let [forced-options {:join? false :websockets {"/ws" websocket-handler}}]
    (jetty/run-jetty web-handler (miss/deep-merge settings forced-options))))