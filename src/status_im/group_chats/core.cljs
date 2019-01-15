(ns status-im.group-chats.core
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as spec]
            [clojure.set :as clojure.set]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.utils.config :as config]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.chat.models.message :as models.message]
            [status-im.contact.core :as models.contact]
            [status-im.native-module.core :as native-module]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.db :as transport.db]
            [status-im.transport.utils :as transport.utils]
            [status-im.transport.message.protocol :as protocol]
            [status-im.transport.message.group-chat :as message.group-chat]
            [status-im.transport.message.public-chat :as transport.public-chat]
            [status-im.transport.chat.core :as transport.chat]
            [status-im.utils.fx :as fx]
            [status-im.chat.models :as models.chat]
            [status-im.accounts.db :as accounts.db]
            [status-im.transport.message.transit :as transit]))

;; Description of the flow:
;; the flow is complicated a bit by 2 asynchronous call to status-go, which might make the logic a bit more opaque.
;; To send a group-membership update, we first build a message.
;; We then sign it with our private key and dispatch it.
;; Conversely when receiving a message, we first extract the public keys from the signature and attach those in the :from field.
;; We then process the events.
;; It is importatn that the from field is not trusted for updates coming from the outside.
;; When messages are coming from the database it can be trustured as already verified by us.

(defn sort-events [events]
  (sort-by :clock-value events))

(defn- event->vector
  "Transform an event in an a vector with keys in alphabetical order"
  [event]
  (mapv
   #(vector % (get event %))
   (sort (keys event))))

(defn get-last-clock-value
  "Given a chat id get the last clock value of an event"
  [cofx chat-id]
  (->> (get-in cofx [:db :chats chat-id :last-clock-value])))

(defn- parse-response [response-js]
  (-> response-js
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn extract-creator
  "Takes a chat as an input, returns the creator"
  [{:keys [membership-updates]}]
  (->> membership-updates
       (filter (fn [{:keys [events]}]
                 (some #(= "chat-created" (:type %)) events)))
       first
       :from))

(defn joined-event? [public-key {:keys [members-joined] :as chat}]
  (contains? members-joined public-key))

(defn joined? [public-key {:keys [group-chat-local-version] :as chat}]
  ;; We consider group chats with local version of 0 as joined for local events
  (or (zero? group-chat-local-version)
      (joined-event? public-key chat)))

(defn creator? [public-key chat]
  (= public-key (extract-creator chat)))

(defn invited? [my-public-key {:keys [contacts]}]
  (contains? contacts my-public-key))

(defn signature-material
  "Transform an update into a signable string"
  [chat-id events]
  (js/JSON.stringify
   (clj->js [(mapv event->vector (sort-events events)) chat-id])))

(defn signature-pairs
  "Transform a bunch of updates into signable pairs to be verified"
  [{:keys [chat-id membership-updates] :as payload}]
  (let [pairs (mapv (fn [{:keys [events signature]}]
                      [(signature-material chat-id events)
                       signature])
                    membership-updates)]
    (js/JSON.stringify (clj->js pairs))))

(defn valid-chat-id?
  ;; We need to make sure the chat-id ends with the admin pk (and it's not the same).
  ;; this is due to prevent an attack whereby a non-admin user would
  ;; send out a message with identical chat-id and themselves as admin to other members,
  ;; who would then have to trust the first of the two messages received, possibly
  ;; resulting in a situation where some of the members in the chat trust a different admin.
  [chat-id admin]
  (and (string/ends-with? chat-id admin)
       (not= chat-id admin)))

(defn valid-event?
  "Check if event can be applied to current group"
  [{:keys [admins contacts]} {:keys [chat-id from member members] :as new-event}]
  (when from
    (condp = (:type new-event)
      "chat-created"   (and (empty? admins)
                            (empty? contacts))
      "name-changed"   (and (admins from)
                            (not (string/blank? (:name new-event))))
      "members-added"   (admins from)
      "member-joined"   (and (contacts member)
                             (= from member))
      "admins-added"    (and (admins from)
                             (clojure.set/subset? members contacts))
      "member-removed" (or
                        ;; An admin removing a member
                        (and (admins from)
                             (not (admins member)))
                        ;; Members can remove themselves
                        (= from member))
      "admin-removed" (and (admins from)
                           (= from member))
      false)))

(defn send-membership-update
  "Send a membership update to all participants but the sender"
  ([cofx payload chat-id]
   (send-membership-update cofx payload chat-id nil))
  ([{:keys [message-id] :as cofx} payload chat-id removed-members]
   (let [chat    (get-in cofx [:db :chats chat-id])
         creator (extract-creator chat)
         members (clojure.set/union (get-in cofx [:db :chats chat-id :contacts])
                                    removed-members)
         {:keys [web3]} (:db cofx)
         current-public-key (accounts.db/current-public-key cofx)
         ;; If a member has joined is listening to the shared topic and we send there
         ;; to ourselves we send always on contact-discovery to make sure all devices
         ;; are informed, in case of dropped messages.
         ;; We check that it has explicitly joined, regardless of the local
         ;; version of the group chat, for backward compatibility
         destinations (map (fn [member]
                             (if (and
                                  config/group-chats-publish-to-topic?
                                  (joined-event? member chat)
                                  (not= creator member)
                                  (not= current-public-key member))
                               {:public-key member
                                :chat chat-id}
                               {:public-key member
                                :chat constants/contact-discovery}))
                           members)]
     (fx/merge
      cofx
      {:shh/send-group-message
       {:web3          web3
        :src           current-public-key
        :dsts          destinations
        :success-event [:transport/message-sent
                        chat-id
                        message-id
                        :group-user-message]
        :payload       payload}}))))

(fx/defn handle-membership-update-received
  "Extract signatures in status-go and act if successful"
  [cofx membership-update signature raw-payload]
  {:group-chats/extract-membership-signature [[membership-update raw-payload signature]]})

(defn chat->group-update
  "Transform a chat in a GroupMembershipUpdate"
  [chat-id {:keys [membership-updates]}]
  (message.group-chat/map->GroupMembershipUpdate. {:chat-id            chat-id
                                                   :membership-updates membership-updates}))

(defn handle-sign-response
  "Callback to dispatch on sign response"
  [payload response-js]
  (let [{:keys [error signature]} (parse-response response-js)]
    (if error
      (re-frame/dispatch [:group-chats.callback/sign-failed  error])
      (re-frame/dispatch [:group-chats.callback/sign-success (assoc payload :signature signature)]))))

(defn add-identities
  "Add verified identities extracted from the signature to the updates"
  [payload identities]
  (update payload :membership-updates (fn [updates]
                                        (map
                                         #(assoc %1 :from (str "0x" %2))
                                         updates
                                         identities))))

(defn handle-extract-signature-response
  "Callback to dispatch on extract signature response"
  [payload raw-payload sender-signature response-js]
  (let [{:keys [error identities]} (parse-response response-js)]
    (if error
      (re-frame/dispatch [:group-chats.callback/extract-signature-failed  error])
      (re-frame/dispatch [:group-chats.callback/extract-signature-success
                          (add-identities payload identities) raw-payload sender-signature]))))

(defn sign-membership [{:keys [chat-id events] :as payload}]
  (native-module/sign-group-membership (signature-material chat-id events)
                                       (partial handle-sign-response payload)))

(defn extract-membership-signature [payload raw-payload sender]
  (native-module/extract-group-membership-signatures
   (signature-pairs payload)
   (partial handle-extract-signature-response payload raw-payload sender)))

(defn- members-added-event [last-clock-value members]
  {:type "members-added"
   :clock-value (utils.clocks/send last-clock-value)
   :members members})

(defn- member-joined-event [last-clock-value member]
  {:type "member-joined"
   :clock-value (utils.clocks/send last-clock-value)
   :member member})

(fx/defn create
  "Format group update message and sign membership"
  [{:keys [db random-guid-generator] :as cofx} group-name]
  (let [my-public-key     (accounts.db/current-public-key cofx)
        chat-id           (str (random-guid-generator) my-public-key)
        selected-contacts (:group/selected-contacts db)
        clock-value       (utils.clocks/send 0)
        create-event      {:type        "chat-created"
                           :name        group-name
                           :clock-value clock-value}
        events            [create-event
                           (members-added-event clock-value selected-contacts)]]

    {:group-chats/sign-membership {:chat-id chat-id
                                   :from   my-public-key
                                   :events events}
     :db (assoc db :group/selected-contacts #{})}))

(fx/defn remove-member
  "Format group update message and sign membership"
  [{:keys [db] :as cofx} chat-id member]
  (let [my-public-key     (accounts.db/current-public-key cofx)
        last-clock-value  (get-last-clock-value cofx chat-id)
        chat              (get-in cofx [:db :chats chat-id])
        remove-event       {:type        "member-removed"
                            :member      member
                            :clock-value (utils.clocks/send last-clock-value)}]
    (when (valid-event? chat (assoc remove-event
                                    :from
                                    my-public-key))
      {:group-chats/sign-membership {:chat-id chat-id
                                     :from    my-public-key
                                     :events  [remove-event]}})))

(fx/defn join-chat
  "Format group update message and sign membership"
  [{:keys [db] :as cofx} chat-id]
  (let [my-public-key     (accounts.db/current-public-key cofx)
        last-clock-value  (get-last-clock-value cofx chat-id)
        chat              (get-in cofx [:db :chats chat-id])
        event             (member-joined-event last-clock-value my-public-key)]
    (when (valid-event? chat (assoc event
                                    :from
                                    my-public-key))
      {:group-chats/sign-membership {:chat-id chat-id
                                     :from    my-public-key
                                     :events  [event]}})))

(fx/defn make-admin
  "Format group update with make admin message and sign membership"
  [{:keys [db] :as cofx} chat-id member]
  (let [my-public-key     (accounts.db/current-public-key cofx)
        last-clock-value  (get-last-clock-value cofx chat-id)
        chat              (get-in cofx [:db :chats chat-id])
        event             {:type        "admins-added"
                           :members     [member]
                           :clock-value (utils.clocks/send last-clock-value)}]
    (when (valid-event? chat (assoc event
                                    :from
                                    my-public-key))
      {:group-chats/sign-membership {:chat-id chat-id
                                     :from    my-public-key
                                     :events  [event]}})))

(fx/defn add-members
  "Add members to a group chat"
  [{{:keys [current-chat-id selected-participants]} :db :as cofx}]
  (let [last-clock-value  (get-last-clock-value cofx current-chat-id)
        events            [(members-added-event last-clock-value selected-participants)]]

    {:group-chats/sign-membership {:chat-id current-chat-id
                                   :from    (accounts.db/current-public-key cofx)
                                   :events  events}}))
(fx/defn remove
  "Remove & leave chat"
  [{:keys [db] :as cofx} chat-id]
  (let [my-public-key (accounts.db/current-public-key cofx)]
    (fx/merge cofx
              (remove-member chat-id my-public-key)
              (models.chat/remove-chat chat-id))))

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
  (let [current-chat-id    (get-in cofx [:db :current-chat-id])
        my-public-key      (get-in db [:account/account :public-key])
        last-clock-value   (get-last-clock-value cofx current-chat-id)
        new-name           (get-in cofx [:db :group-chat-profile/profile :name])
        name-changed-event {:type        "name-changed"
                            :name        new-name
                            :clock-value (utils.clocks/send last-clock-value)}]
    (when (valid-name? new-name)
      (fx/merge cofx
                {:db                          (assoc db
                                                     :group-chat-profile/editing?
                                                     false)
                 :group-chats/sign-membership {:chat-id current-chat-id
                                               :from    my-public-key
                                               :events  [name-changed-event]}}))))

(defn process-event
  "Add/remove an event to a group, carrying the clock-value at which it happened"
  [group {:keys [type member members chat-id clock-value from name] :as event}]
  (if (valid-event? group event)
    (case type
      "chat-created"   {:name       name
                        :created-at clock-value
                        :admins         #{from}
                        :members-joined #{from}
                        :contacts       #{from}}
      "name-changed"   (assoc group
                              :name name
                              :name-changed-by from
                              :name-changed-at clock-value)
      "members-added"  (as-> group $
                         (update $ :contacts clojure.set/union (set members))
                         (reduce (fn [acc member] (assoc-in acc [member :added] clock-value)) $ members))
      "member-joined"  (-> group
                           (update  :members-joined conj member)
                           (assoc-in  [member :joined] clock-value))
      "admins-added"   (as-> group $
                         (update $ :admins clojure.set/union (set members))
                         (reduce (fn [acc member] (assoc-in acc [member :admin-added] clock-value)) $ members))
      "member-removed" (-> group
                           (update :contacts disj member)
                           (update :admins disj member)
                           (update :members-joined disj member)
                           (assoc-in [member :removed] clock-value))
      "admin-removed"  (-> group
                           (update :admins disj member)
                           (assoc-in [member :admin-removed] clock-value)))
    group))

(defn build-group
  "Given a list of already authenticated events build a group with contats/admin"
  [events]
  (->> events
       sort-events
       (reduce
        process-event
        {:admins #{}
         :members-joined #{}
         :contacts #{}})))

(defn membership-changes->system-messages [cofx
                                           clock-values
                                           {:keys [chat-id
                                                   chat-name
                                                   creator
                                                   members-added
                                                   members-joined
                                                   admins-added
                                                   name-changed?
                                                   members-removed]}]
  (let [get-contact         (partial models.contact/build-contact cofx)
        format-message      (fn [contact text clock-value]
                              {:chat-id     chat-id
                               :content     {:text text}
                               :clock-value clock-value
                               :from        (:public-key contact)})
        creator-contact     (when creator (get-contact creator))
        name-changed-author (when name-changed? (get-contact (:name-changed-by clock-values)))
        admins-added        (map
                             get-contact
                             (disj admins-added creator))
        contacts-added      (map
                             get-contact
                             (disj members-added creator))
        contacts-joined     (map
                             get-contact
                             (disj members-joined creator))
        contacts-removed    (map
                             get-contact
                             members-removed)]
    (cond-> []
      creator-contact (conj (format-message creator-contact
                                            (i18n/label :t/group-chat-created
                                                        {:name   chat-name
                                                         :member (:name creator-contact)})
                                            (:created-at clock-values)))
      name-changed?  (conj (format-message name-changed-author
                                           (i18n/label :t/group-chat-name-changed
                                                       {:name   chat-name
                                                        :member (:name name-changed-author)})
                                           (:name-changed-at clock-values)))
      (seq members-added) (concat (map #(format-message
                                         %
                                         (i18n/label :t/group-chat-member-added {:member (:name %)})
                                         (get-in clock-values [(:public-key %) :added]))
                                       contacts-added))
      (seq members-joined) (concat (map #(format-message
                                          %
                                          (i18n/label :t/group-chat-member-joined {:member (:name %)})
                                          (get-in clock-values [(:public-key %) :joined]))
                                        contacts-joined))
      (seq admins-added) (concat (map #(format-message
                                        %
                                        (i18n/label :t/group-chat-admin-added {:member (:name %)})
                                        (get-in clock-values [(:public-key %) :admin-added]))
                                      admins-added))
      (seq members-removed) (concat (map #(format-message
                                           %
                                           (i18n/label :t/group-chat-member-removed {:member (:name %)})
                                           (get-in clock-values [(:public-key %) :removed]))
                                         contacts-removed)))))

(fx/defn add-system-messages [cofx chat-id previous-chat clock-values]
  (let [current-chat (get-in cofx [:db :chats chat-id])
        current-public-key (get-in cofx [:db :current-public-key])
        name-changed?  (and (seq previous-chat)
                            (not= (:name previous-chat) (:name current-chat)))
        members-added (clojure.set/difference (:contacts current-chat) (:contacts previous-chat))
        members-joined (clojure.set/difference (:members-joined current-chat) (:members-joined previous-chat))
        members-removed (clojure.set/difference (:contacts previous-chat) (:contacts current-chat))
        admins-added  (clojure.set/difference (:admins current-chat) (:admins previous-chat))
        membership-changes (cond-> {:chat-id         chat-id
                                    :name-changed?   name-changed?
                                    :chat-name       (:name current-chat)
                                    :admins-added    admins-added
                                    :members-added   members-added
                                    :members-joined  members-joined
                                    :members-removed members-removed}
                             (nil? previous-chat)
                             (assoc :creator (extract-creator current-chat)))]
    (when (or name-changed?
              (seq admins-added)
              (seq members-added)
              (seq members-joined)
              (seq members-removed))
      (->> membership-changes
           (membership-changes->system-messages cofx clock-values)
           (models.message/add-system-messages cofx)))))

(defn- unwrap-events
  "Flatten all events, denormalizing from field"
  [all-updates]
  (mapcat
   (fn [{:keys [events from]}]
     (map #(assoc % :from from) events))
   all-updates))

(defn get-inviter-pk [my-public-key {:keys [membership-updates]}]
  (->> membership-updates
       unwrap-events
       (keep (fn [{:keys [from type members]}]
               (when (and (= type "members-added")
                          (contains? members my-public-key))
                 from)))
       last))

(fx/defn set-up-topic [cofx chat-id previous-chat]
  (let [my-public-key (accounts.db/current-public-key cofx)
        new-chat (get-in cofx [:db :chats chat-id])]
    ;; If we left the chat, teardown, otherwise upsert
    (if (and (joined? my-public-key previous-chat)
             (not (joined? my-public-key new-chat)))
      (transport.chat/unsubscribe-from-chat cofx chat-id)
      (transport.public-chat/join-group-chat cofx chat-id))))

(fx/defn handle-membership-update
  "Upsert chat and receive message if valid"
  ;; Care needs to be taken here as chat-id is not coming from a whisper filter
  ;; so can be manipulated by the sending user.
  [cofx {:keys [chat-id
                message
                membership-updates] :as membership-update}
   raw-payload
   sender-signature]
  (let [dev-mode? (get-in cofx [:db :account/account :dev-mode?])]
    (when (valid-chat-id? chat-id (extract-creator membership-update))
      (let [previous-chat (get-in cofx [:db :chats chat-id])
            all-updates (clojure.set/union (set (:membership-updates previous-chat))
                                           (set (:membership-updates membership-update)))
            my-public-key (accounts.db/current-public-key cofx)
            unwrapped-events (unwrap-events all-updates)
            new-group (build-group unwrapped-events)
            member? (contains? (:contacts new-group) my-public-key)]
        (fx/merge cofx
                  (models.chat/upsert-chat {:chat-id                  chat-id
                                            :name                     (:name new-group)
                                            :group-chat-local-version (get previous-chat :group-chat-local-version 1)
                                            :is-active                (or member?
                                                                          (get previous-chat :is-active true))
                                            :group-chat               true
                                            :membership-updates       (into [] all-updates)
                                            :admins                   (:admins new-group)
                                            :members-joined           (:members-joined new-group)
                                            :contacts                 (:contacts new-group)})
                  (add-system-messages chat-id previous-chat new-group)
                  (set-up-topic chat-id previous-chat)
                  #(when (and message
                              ;; don't allow anything but group messages
                              (instance? protocol/Message message)
                              (= :group-user-message (:message-type message)))
                     (protocol/receive message chat-id sender-signature nil
                                       (assoc % :js-obj #js {:payload raw-payload}))))))))

(defn handle-sign-success
  "Upsert chat and send signed payload to group members"
  [{:keys [db] :as cofx} {:keys [chat-id] :as signed-events}]
  (let [old-chat      (get-in db [:chats chat-id])
        updated-chat  (update old-chat :membership-updates conj signed-events)
        my-public-key (accounts.db/current-public-key cofx)
        group-update  (chat->group-update chat-id updated-chat)
        new-group-fx  (handle-membership-update group-update nil my-public-key)
        ;; We need to send to users who have been removed as well
        recipients    (clojure.set/union
                       (:contacts old-chat)
                       (get-in new-group-fx [:db :chats chat-id :contacts]))]
    (fx/merge cofx
              new-group-fx
              #(when (get-in % [:db :chats chat-id :is-active])
                 (models.chat/navigate-to-chat % chat-id {:navigation-reset? true}))
              #(send-membership-update % group-update chat-id recipients))))

(re-frame/reg-fx
 :group-chats/sign-membership
 sign-membership)

(re-frame/reg-fx
 :group-chats/extract-membership-signature
 (fn [signatures]
   (doseq [[payload raw-payload sender] signatures]
     (extract-membership-signature payload raw-payload sender))))
