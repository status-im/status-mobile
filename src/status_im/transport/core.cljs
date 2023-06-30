(ns ^{:doc "API to init and stop whisper messaging"} status-im.transport.core
  (:require [re-frame.core :as re-frame]
            [status-im.pairing.core :as pairing]
            [status-im.stickers.core :as stickers]
            status-im.transport.shh
            [utils.re-frame :as rf]
            [status-im.utils.universal-links.core :as universal-links]
            [taoensso.timbre :as log]))

(rf/defn set-node-info
  {:events [:transport.callback/node-info-fetched]}
  [{:keys [db]} node-info]
  {:db (assoc db :node-info node-info)})

(rf/defn fetch-node-info-fx
  [_]
  {:json-rpc/call [{:method     "admin_nodeInfo"
                    :on-success #(re-frame/dispatch [:transport.callback/node-info-fetched %])
                    :on-error   #(log/error "node-info: failed error" %)}]})

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

(rf/defn start-messenger
  "We should only start receiving messages/processing topics once all the
  initializiation is completed, otherwise we might receive messages/topics
  when the state has not been properly initialized."
  [_]
  {:json-rpc/call [{:method     "wakuext_startMessenger"
                    :on-success #(re-frame/dispatch [::messenger-started %])
                    :on-error   #(log/error "failed to start messenger")}]})

(rf/defn messenger-started
  {:events [::messenger-started]}
  [{:keys [db] :as cofx} {:keys [mailservers] :as response}]
  (log/info "Messenger started")
  (let [new-profile? (get db :profile/new?)]
    (rf/merge cofx
              {:db (-> db
                       (assoc :messenger/started? true)
                       (add-mailservers mailservers))}
              (fetch-node-info-fx)
              (pairing/init)
              (stickers/load-packs)
              (when-not new-profile?
                (universal-links/process-stored-event)))))
