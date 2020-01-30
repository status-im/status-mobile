(ns ^{:doc "API to init and stop whisper messaging"}
 status-im.transport.core
  (:require
   [re-frame.core :as re-frame]
   [status-im.ethereum.json-rpc :as json-rpc]
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

(fx/defn init-messenger
  "We should only start receiving messages/processing topics once all the
  initializiation is completed, otherwise we might receive messages/topics
  when the state has not been properly initialized."
  [cofx]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_startMessenger"
                               "shhext_startMessenger")
                     :on-success #(do
                                    (log/debug "messenger initialized")
                                    (re-frame/dispatch [::init-whisper]))
                     :on-failure #(log/error "failed to init messenger")}]})

(fx/defn init-whisper
  "Initialises whisper protocol by:
  - (optionally) initializing mailserver"
  {:events [::init-whisper]}
  [cofx]
  (log/debug "Initializing whisper")
  (fx/merge cofx
            (fetch-node-info-fx)
            (pairing/init)
            (publisher/start-fx)
            (when-not config/nimbus-enabled? (mailserver/initialize-mailserver))))

(fx/defn stop-whisper
  "Stops whisper protocol"
  [cofx]
  (publisher/stop-fx cofx))
