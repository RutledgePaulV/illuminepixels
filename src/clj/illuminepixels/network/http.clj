(ns illuminepixels.network.http
  (:require [compojure.core :refer :all]
            [ring.util.response :as response]
            [compojure.route :refer [resources]]))


(defroutes web-routes

  (GET "/" []
    (response/resource-response "index.html" {:root "public"}))

  (resources "/")

  (ANY "/**" []
    (response/resource-response "index.html" {:root "public"})))