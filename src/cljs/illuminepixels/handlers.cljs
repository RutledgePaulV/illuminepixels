(ns illuminepixels.handlers
  (:require [haslett.format :as fmt]
            [haslett.client :as ws]
            [re-frame.core :as re-frame]
            [cljs.core.async :as async]
            [illuminepixels.events :as events]))


(defn websocket-cofx [{:keys [url] :as config}]
  (async/go
    (let [{:keys [socket close-status source sink]}
          (async/<! (ws/connect url {:format fmt/edn}))
          multiplexed (async/mult source)]
      (re-frame/dispatch [::events/websocket-connected {:socket socket :source multiplexed :sink sink}])
      (async/go
        (let [{:keys [code reason]} (async/<! close-status)]
          (.error js/console "websocket error" code reason)
          (re-frame/dispatch [::events/websocket-disconnected])
          (websocket-cofx config))))))

(re-frame/reg-fx :websocket websocket-cofx)
