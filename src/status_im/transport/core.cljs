(ns ^{:doc "API to init and stop whisper messaging"}
 status-im.transport.core
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.native-module.core :as status]
            [status-im.pairing.core :as pairing]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            status-im.transport.shh
            [taoensso.timbre :as log]
            [status-im.utils.universal-links.core :as universal-links]))

(fx/defn set-node-info
  {:events [:transport.callback/node-info-fetched]}
  [{:keys [db]} node-info]
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

(defn add-mailservers
  [db mailservers]
  (reduce (fn [db {:keys [fleet id name] :as mailserver}]
            (let [updated-mailserver
                  (-> mailserver
                      (update :id keyword)
                      (assoc :name (if (seq name) name id))
                      (dissoc :fleet))]
              (assoc-in db
                        [:mailserver/mailservers (keyword fleet) (keyword id)]
                        updated-mailserver)))
          db
          mailservers))

(fx/defn start-messenger
  "We should only start receiving messages/processing topics once all the
  initializiation is completed, otherwise we might receive messages/topics
  when the state has not been properly initialized."
  [_]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "startMessenger")
                     :on-success #(re-frame/dispatch [::messenger-started %])
                     :on-failure #(log/error "failed to start messenger")}]})

(fx/defn messenger-started
  {:events [::messenger-started]}
  [{:keys [db] :as cofx} {:keys [mailservers] :as response}]
  (log/info "Messenger started")
  (fx/merge cofx
            {:db (-> db
                     (assoc :messenger/started? true)
                     (add-mailservers mailservers))}
            (fetch-node-info-fx)
            (pairing/init)
            (universal-links/process-stored-event)))
