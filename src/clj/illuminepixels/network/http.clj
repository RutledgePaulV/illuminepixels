(ns illuminepixels.network.http
  (:require [compojure.core :refer :all]
            [ring.util.response :as response]
            [compojure.route :refer [resources]]))


(defroutes web-routes

  (GET "/healthz" []
    (-> (pr-str {:healthy true})
        (response/response)
        (response/content-type "application/edn")))

  (GET "/" []
    (response/resource-response "index.html" {:root "public"}))

  (resources "/")

  (ANY "/**" [] (response/not-found "Not found.")))