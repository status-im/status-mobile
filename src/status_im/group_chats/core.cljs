(ns status-im.group-chats.core
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.utils.config :as config]
            [status-im.native-module.core :as native-module]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.db :as transport.db]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.core :as protocol.message]
            [status-im.transport.message.v1.core :as transport]
            [status-im.transport.message.v1.protocol :as transport.protocol]
            [status-im.utils.fx :as fx]
            [status-im.chat.models :as models.chat]))

(defn- parse-response [response-js]
  (-> response-js
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn signature-material [{:keys [chat-id admin participants]}]
  (apply str
         (concat (sort participants)
                 admin
                 chat-id)))

(defn signature-pairs [{:keys [admin signature] :as payload}]
  (js/JSON.stringify (clj->js [[(signature-material payload)
                                signature
                                (subs admin 2)]])))

(defn valid-chat-id?
  ;; We need to make sure the chat-id ends with the admin pk (and it's not the same).
  ;; this is due to prevent an attack whereby a non-admin user would
  ;; send out a message with identical chat-id and themselves as admin to other members,
  ;; who would then have to trust the first of the two messages received, possibly
  ;; resulting in a situation where some of the members in the chat trust a different admin.
  [chat-id admin]
  (and (string/ends-with? chat-id admin)
       (not= chat-id admin)))

(defn wrap-group-message
  "Wrap a group message in a membership update"
  [cofx chat-id message]
  (when-let [chat (get-in cofx [:db :chats chat-id])]
    (transport/map->GroupMembershipUpdate.
     {:chat-id      chat-id
      :chat-name    (:name chat)
      :admin        (:group-admin chat)
      :participants (:contacts chat)
      :signature    (:membership-signature chat)
      :version      (:membership-version chat)
      :message      message})))

(fx/defn update-membership
  "Upsert chat when version is greater or not existing"
  [cofx previous-chat {:keys [chat-id
                              chat-name
                              participants
                              leaves
                              admin
                              signature
                              version]}]
  (when
   (or
    (nil? previous-chat)
    (< (:membership-version previous-chat)
       version))

    (models.chat/upsert-chat cofx
                             {:chat-id              chat-id
                              :name                 chat-name
                              :is-active            (get previous-chat :is-active true)
                              :group-chat           true
                              :group-admin          admin
                              :contacts             participants
                              :membership-signature signature
                              :membership-version   version})))

(defn send-membership-update
  "Send a membership update to all participants but the sender"
  [cofx payload chat-id]
  (let [{:keys [participants]} payload
        {:keys [current-public-key web3]} (:db cofx)]
    (fx/merge
     cofx
     {:shh/send-group-message {:web3          web3
                               :src           current-public-key
                               :dsts          (disj participants current-public-key)
                               :success-event [:transport/set-message-envelope-hash
                                               chat-id
                                               (transport.utils/message-id (:message payload))
                                               :group-user-message]
                               :payload       payload}})))

(defn send-group-leave [payload chat-id cofx]
  (transport.protocol/send cofx
                           {:chat-id       chat-id
                            :payload       payload
                            :success-event [:group/unsubscribe-from-chat chat-id]}))

(fx/defn handle-membership-update-received
  "Verify signatures in status-go and act if successful"
  [cofx membership-update signature]
  {:group-chats/verify-membership-signature [[membership-update signature]]})

(fx/defn handle-membership-update
  "Upsert chat and receive message if valid"
  ;; Care needs to be taken here as chat-id is not coming from a whisper filter
  ;; so can be manipulated by the sending user.
  [cofx {:keys [chat-id
                chat-name
                participants
                signature
                leaves
                message
                admin
                version] :as membership-update}
   sender-signature]
  (when (and config/group-chats-enabled?
             (valid-chat-id? chat-id admin))
    (let [previous-chat (get-in cofx [:db :chats chat-id])]
      (fx/merge cofx
                (update-membership previous-chat membership-update)
                #(when (and message
                            ;; don't allow anything but group messages
                            (instance? transport.protocol/Message message)
                            (= :group-user-message (:message-type message)))
                   (protocol.message/receive message chat-id sender-signature nil %))))))

(defn handle-sign-success
  "Upsert chat and send signed payload to group members"
  [{:keys [db] :as cofx} {:keys [chat-id] :as group-update}]
  (let [my-public-key (:current-public-key db)]
    (fx/merge cofx
              (models.chat/navigate-to-chat chat-id {:navigation-reset? true})
              (handle-membership-update group-update my-public-key)
              #(protocol.message/send group-update chat-id %))))

(defn handle-sign-response
  "Callback to dispatch on sign response"
  [payload response-js]
  (let [{:keys [error signature]} (parse-response response-js)]
    (if error
      (re-frame/dispatch [:group-chats.callback/sign-failed  error])
      (re-frame/dispatch [:group-chats.callback/sign-success (assoc payload :signature signature)]))))

(defn handle-verify-signature-response
  "Callback to dispatch on verify signature response"
  [payload sender-signature response-js]
  (let [{:keys [error]} (parse-response response-js)]
    (if error
      (re-frame/dispatch [:group-chats.callback/verify-signature-failed  error])
      (re-frame/dispatch [:group-chats.callback/verify-signature-success payload sender-signature]))))

(defn sign-membership [payload]
  (native-module/sign-group-membership (signature-material payload)
                                       (partial handle-sign-response payload)))

(defn verify-membership-signature [signatures]
  (doseq [[payload sender-signature] signatures]
    (native-module/verify-group-membership-signatures (signature-pairs payload)
                                                      (partial handle-verify-signature-response payload sender-signature))))

(fx/defn create
  "Format group update message and sign membership"
  [{:keys [db random-guid-generator] :as cofx} group-name]
  (let [my-public-key     (:current-public-key db)
        chat-id           (str (random-guid-generator) my-public-key)
        selected-contacts (conj (:group/selected-contacts db)
                                my-public-key)
        group-update      (transport/map->GroupMembershipUpdate
                           {:chat-id      chat-id
                            :chat-name    group-name
                            :admin        my-public-key
                            :participants selected-contacts
                            :version      1})]
    {:group-chats/sign-membership group-update
     :db (assoc db :group/selected-contacts #{})}))

(defn- valid-name? [name]
  (spec/valid? :profile/name name))

(fx/defn update-name [{:keys [db]} name]
  {:db (-> db
           (assoc-in [:group-chat-profile/profile :valid-name?] (valid-name? name))
           (assoc-in [:group-chat-profile/profile :name] name))})

(fx/defn handle-name-changed
  "Store name in profile scratchpad"
  [cofx new-chat-name]
  (update-name cofx new-chat-name))

(fx/defn save
  "Save chat from edited profile"
  [{:keys [db] :as cofx}]
  (let [current-chat-id (get-in cofx [:db :current-chat-id])
        new-name (get-in cofx [:db :group-chat-profile/profile :name])]
    (when (valid-name? new-name)
      (fx/merge cofx
                {:db (assoc db :group-chat-profile/editing? false)}
                (models.chat/upsert-chat {:chat-id current-chat-id
                                          :name new-name})))))
(re-frame/reg-fx
 :group-chats/sign-membership
 sign-membership)

(re-frame/reg-fx
 :group-chats/verify-membership-signature
 (fn [signatures]
   (verify-membership-signature signatures)))
