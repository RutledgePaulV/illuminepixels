(ns illuminepixels.features.peers
  (:require [clojure.core.async :as async]
            [websocket-layer.core :as wl]))


(defonce peers (atom {}))

(add-watch peers :reactor
  (fn [k r o n]
    (when (not= o n)
      (doseq [[k subscriptions] n sub subscriptions]
        (when (not= (count subscriptions) (count (get o k #{})))
          (async/put! sub {:peers (count subscriptions)}))))))

(defmethod wl/handle-subscription :peers [{:keys [key millis] :or {millis 1000}}]
  (let [response (async/chan)]
    (wl/on-chan-close response (fn [] (swap! peers update key disj response)))
    (swap! peers update key (fnil conj #{}) response)
    response))

(defmethod wl/handle-subscription :ping [{:keys [millis] :or {millis 10000}}]
  (let [response (async/chan)]
    (async/go-loop [counter 0]
      (when (async/>! response {:pong true :count counter})
        (async/<! (async/timeout millis))
        (recur (inc counter))))
    response))