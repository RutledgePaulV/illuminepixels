(ns illuminepixels.features.peers
  (:require [illuminepixels.utils :as utils]
            [clojure.core.async :as async]
            [illuminepixels.network.api :as api]))


(defonce peers (atom {}))

(add-watch peers :reactor
  (fn [k r o n]
    (when (not= o n)
      (doseq [[route subscriptions] n sub subscriptions]
        (when (not= (count subscriptions) (count (get o route #{})))
          (async/put! sub {:peers (count subscriptions)}))))))

(defmethod api/handle-subscribe :peers [{:keys [route millis] :or {millis 1000}}]
  (let [response (async/chan)]
    (utils/on-close response (fn [] (swap! peers update route disj response)))
    (swap! peers update route (fnil conj #{}) response)
    response))