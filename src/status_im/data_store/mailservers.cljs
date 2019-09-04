(ns status-im.data-store.mailservers
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn mailserver-request-gaps->rpc
  [{:keys [chat-id] :as gap}]
  (-> gap
      (assoc :chatId chat-id)
      (dissoc :chat-id)))

(fx/defn load-gaps
  [cofx chat-id success-fn]
  {::json-rpc/call [{:method "mailservers_getMailserverRequestGaps"
                     :params [chat-id]
                     :on-success #(let [indexed-gaps (reduce (fn [acc {:keys [id] :as g}]
                                                               (assoc acc id g))
                                                             {}
                                                             %)]
                                    (success-fn chat-id indexed-gaps))
                     :on-failure #(log/error "failed to fetch gaps" %)}]})

(fx/defn save-gaps
  [cofx gaps]
  {::json-rpc/call [{:method "mailservers_addMailserverRequestGaps"
                     :params [(map mailserver-request-gaps->rpc gaps)]
                     :on-success #(log/info "saved gaps successfully")
                     :on-failure #(log/error "failed to save gap" %)}]})

(fx/defn delete-gaps
  [cofx ids]
  {::json-rpc/call [{:method "mailservers_deleteMailserverRequestGaps"
                     :params [ids]
                     :on-success #(log/info "deleted gaps successfully")
                     :on-failure #(log/error "failed to delete gap" %)}]})

(fx/defn delete-gaps-by-chat-id
  [cofx chat-id]
  {::json-rpc/call [{:method "mailservers_deleteMailserverRequestGapsByChatID"
                     :params [chat-id]
                     :on-success #(log/info "deleted gaps successfully")
                     :on-failure #(log/error "failed to delete gap" %)}]})

(defn save-chat-requests-range
  [chat-requests-range]
  (fn [realm]
    (log/debug "saving ranges" chat-requests-range)
    (core/create realm :chat-requests-range chat-requests-range true)))

(re-frame/reg-cofx
 :data-store/all-chat-requests-ranges
 (fn [cofx _]
   (assoc cofx :data-store/mailserver-ranges
          (reduce (fn [acc {:keys [chat-id] :as range}]
                    (assoc acc chat-id range))
                  {}
                  (-> @core/account-realm
                      (core/get-all :chat-requests-range)
                      (core/all-clj :chat-requests-range))))))

(defn delete-range
  [chat-id]
  (fn [realm]
    (log/debug "deleting range" chat-id)
    (core/delete realm
                 (core/get-by-field realm :chat-requests-range :chat-id chat-id))))
