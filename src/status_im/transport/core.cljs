(ns ^{:doc "API to init and stop whisper messaging"}
 status-im.transport.core
  (:require
   [re-frame.core :as re-frame]
   [status-im.native-module.core :as status]
   [status-im.mailserver.core :as mailserver]
   [status-im.transport.message.core :as message]
   [status-im.transport.filters.core :as transport.filters]
   [status-im.pairing.core :as pairing]
   [status-im.utils.publisher :as publisher]
   [status-im.utils.fx :as fx]
   [status-im.utils.handlers :as handlers]
   [taoensso.timbre :as log]
   status-im.transport.shh
   [status-im.utils.config :as config]))

(defn set-node-info [{:keys [db]} node-info]
  {:db (assoc db :node-info node-info)})

(defn fetch-node-info []
  (let [args    {:jsonrpc "2.0"
                 :id      2
                 :method  "admin_nodeInfo"}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-private-rpc payload
                             (handlers/response-handler #(re-frame/dispatch [:transport.callback/node-info-fetched %])
                                                        #(log/error "node-info: failed error" %)))))

(re-frame/reg-fx
 ::fetch-node-info
 (fn []
   (fetch-node-info)))

(fx/defn fetch-node-info-fx [cofx]
  {::fetch-node-info []})

(fx/defn init-whisper
  "Initialises whisper protocol by:
  - (optionally) initializing mailserver"
  [{:keys [db web3 all-installations] :as cofx}]
  (fx/merge cofx
            (fetch-node-info-fx)
            (transport.filters/load-filters)
            (pairing/init all-installations)
            (publisher/start-fx)
            (mailserver/connect-to-mailserver)
            (message/resend-contact-messages [])))

(fx/defn stop-whisper
  "Stops whisper protocol"
  [cofx]
  (publisher/stop-fx cofx))
