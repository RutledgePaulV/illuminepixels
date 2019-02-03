(ns illuminepixels.cofx
  (:require [haslett.format :as fmt]
            [haslett.client :as ws]
            [re-frame.core :as rf]
            [cljs.core.async :as async]
            [illuminepixels.events :as events]))


(defn websocket-cofx [{:keys [url] :as config}]
  (async/go
    (let [{:keys [socket close-status source sink]}
          (async/<! (ws/connect url {:format fmt/transit}))
          multiplexed (async/mult source)]
      ; start the heartbeat
      (async/put! sink {:proto :subscription :data {:kind :ping} :id (random-uuid)})
      (rf/dispatch [::events/websocket-connected {:socket socket :source multiplexed :sink sink}])
      (async/go
        (let [{:keys [code reason]} (async/<! close-status)]
          (.error js/console "websocket error" code reason)
          (rf/dispatch [::events/websocket-disconnected])
          (rf/dispatch [::events/websocket-connect config]))))))

(defn websocket-msg-cofx [{:keys [sink message]}]
  (async/put! sink {:proto :push :data message :id (random-uuid)}))

(rf/reg-fx :websocket websocket-cofx)

(rf/reg-fx :ws-message websocket-msg-cofx)