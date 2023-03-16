(ns status-im2.subs.chat.chats
  (:require [clojure.string :as string]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.add-new.db :as db]
            [status-im.chat.models.mentions :as mentions]
            [status-im.communities.core :as communities]
            [status-im.group-chats.core :as group-chat]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.image-server :as image-server]
            [status-im2.config :as config]
            [status-im2.constants :as constants]
            [status-im2.contexts.chat.events :as chat.events]
            [utils.i18n :as i18n]))

(re-frame/reg-sub
 :chats/chat
 :<- [:chats/chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(re-frame/reg-sub
 :community-id-by-chat-id
 (fn [[_ chat-id]]
   [(re-frame/subscribe [:chats/chat chat-id])])
 (fn [[chat]]
   (:community-id chat)))

(re-frame/reg-sub
 :chats/by-community-id
 :<- [:chats/chats]
 (fn [chats [_ community-id]]
   (->> chats
        (keep (fn [[_ chat]]
                (when (= (:community-id chat) community-id)
                  chat)))
        (sort-by :timestamp >))))

(re-frame/reg-sub
 :chats/with-empty-category-by-community-id
 (fn [[_ community-id]]
   [(re-frame/subscribe [:chats/by-community-id community-id])
    (re-frame/subscribe [:communities/community-chats community-id])])
 (fn [[chats comm-chats] [_ community-id]]
   (filter #(string/blank? (get-in comm-chats
                                   [(string/replace (:chat-id %) community-id "") :categoryID]))
           chats)))

(re-frame/reg-sub
 :chats/sorted-categories-by-community-id
 (fn [[_ community-id]]
   [(re-frame/subscribe [:chats/by-community-id community-id])
    (re-frame/subscribe [:communities/community-chats community-id])])
 (fn [[chats comm-chats] [_ community-id]]
   (let [chat-cat (into {}
                        (map (fn [{:keys [id categoryID position]}]
                               {(str community-id id) {:categoryID categoryID
                                                       :position   position}})
                             (vals comm-chats)))]
     (group-by :categoryID
               (sort-by :position
                        (map #(cond-> (merge % (chat-cat (:chat-id %)))
                                (= community-id constants/status-community-id)
                                (assoc :color colors/blue))
                             chats))))))

(re-frame/reg-sub
 :chats/category-by-chat-id
 (fn [[_ community-id _]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [chats categories]}] [_ community-id chat-id]]
   (get categories (get-in chats [(string/replace chat-id community-id "") :categoryID]))))

(re-frame/reg-sub
 :chats/community-chat-by-id
 (fn [[_ community-id _]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [chats]}] [_ community-id chat-id]]
   (get chats (string/replace chat-id community-id ""))))

(re-frame/reg-sub
 :chats/home-list-chats
 :<- [:chats/chats]
 :<- [:chats-home-list]
 (fn [[chats active-chats]]
   (reduce #(if-let [item (get chats %2)]
              (conj %1 item)
              %1)
           []
           active-chats)))

(re-frame/reg-sub
 :chat-by-id
 :<- [:chats/chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(re-frame/reg-sub
 :chats/synced-from
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chat-by-id chat-id]))
 (fn [{:keys [synced-from]}]
   synced-from))

(re-frame/reg-sub
 :chats/muted
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chat-by-id chat-id]))
 (fn [{:keys [muted]}]
   muted))

(re-frame/reg-sub
 :chats/chat-type
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chat-by-id chat-id]))
 (fn [{:keys [chat-type]}]
   chat-type))

(re-frame/reg-sub
 :chats/joined
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chat-by-id chat-id]))
 (fn [{:keys [joined]}]
   joined))

(re-frame/reg-sub
 :chats/synced-to-and-from
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chat-by-id chat-id]))
 (fn [chat]
   (select-keys chat [:synced-to :synced-from])))

(re-frame/reg-sub
 :chats/current-raw-chat
 :<- [:chats/chats]
 :<- [:chats/current-chat-id]
 (fn [[chats current-chat-id]]
   (get chats current-chat-id)))

(re-frame/reg-sub
 :chats/current-chat-input
 :<- [:chats/current-chat-id]
 :<- [:chat/inputs]
 (fn [[chat-id inputs]]
   (get inputs chat-id)))

(re-frame/reg-sub
 :chats/sending-image
 :<- [:chats/current-chat-id]
 :<- [:chat/inputs]
 (fn [[chat-id inputs]]
   (get-in inputs [chat-id :metadata :sending-image])))

(re-frame/reg-sub
 :chats/timeline-chat-input-text
 :<- [:chats/timeline-chat-input]
 (fn [input]
   (:input-text input)))

(re-frame/reg-sub
 :chats/current-chat-membership
 :<- [:chats/current-chat-id]
 :<- [:chat/memberships]
 (fn [[chat-id memberships]]
   (get memberships chat-id)))

(re-frame/reg-sub
 :chats/current-chat
 :<- [:chats/current-raw-chat]
 :<- [:multiaccount/public-key]
 :<- [:communities/current-community]
 :<- [:contacts/blocked-set]
 :<- [:contacts/contacts-raw]
 :<- [:chat/inputs]
 (fn [[{:keys [group-chat chat-id] :as current-chat} my-public-key community blocked-users-set contacts
       inputs]]
   (when current-chat
     (cond-> current-chat
       (chat.events/public-chat? current-chat)
       (assoc :able-to-send-message? true)

       (and (chat.events/group-chat? current-chat)
            (group-chats.db/member? my-public-key current-chat))
       (assoc :able-to-send-message? true
              :member?               true)

       (and (chat.events/community-chat? current-chat)
            (communities/can-post? community my-public-key (:chat-id current-chat)))
       (assoc :able-to-send-message? true)

       (not group-chat)
       (assoc
        :contact-request-state (get-in contacts [chat-id :contact-request-state])
        :able-to-send-message?
        (and
         (or
          (get-in inputs [chat-id :metadata :sending-contact-request])
          (= constants/contact-request-state-mutual
             (get-in contacts [chat-id :contact-request-state])))
         (not (contains? blocked-users-set chat-id))))))))

(re-frame/reg-sub
 :chats/current-chat-chat-view
 :<- [:chats/current-chat]
 (fn [current-chat]
   (select-keys current-chat
                [:chat-id
                 :able-to-send-message?
                 :group-chat
                 :admins
                 :invitation-admin
                 :public?
                 :chat-type
                 :color
                 :contact-request-state
                 :chat-name
                 :synced-to
                 :synced-from
                 :community-id
                 :emoji])))

(re-frame/reg-sub
 :chats/current-chat-message-list-view-context
 :<- [:chats/current-chat-chat-view]
 :<- [:communities/current-community]
 :<- [:multiaccount/public-key]
 (fn [[current-chat current-community current-public-key] [_ in-pinned-view?]]
   (let [{:keys [group-chat chat-id public? admins space-keeper able-to-send-message?]}
         current-chat

         {:keys [can-delete-message-for-everyone? admin-settings]}
         current-community

         {:keys [pin-message-all-members-enabled?]} admin-settings
         community? (some? current-community)
         group-admin? (contains? admins current-public-key)
         community-admin? (get current-community :admin false)

         message-pin-enabled
         (cond public?          false
               (not group-chat) true ; one to one chat
               ;; in public group or community
               group-chat       (or group-admin?
                                    pin-message-all-members-enabled?
                                    community-admin?)
               :else            false)]
     {:group-chat                       group-chat
      :group-admin?                     group-admin?
      :public?                          public?
      :community?                       community?
      :community-admin?                 community-admin?
      :current-public-key               current-public-key
      :space-keeper                     space-keeper
      :chat-id                          chat-id
      :in-pinned-view?                  (boolean in-pinned-view?)
      :able-to-send-message?            able-to-send-message?
      :message-pin-enabled              message-pin-enabled
      :can-delete-message-for-everyone? can-delete-message-for-everyone?})))

(re-frame/reg-sub
 :current-chat/metadata
 :<- [:chats/current-raw-chat]
 (fn [current-chat]
   (select-keys current-chat
                [:community-id
                 :contacts
                 :public?
                 :group-chat
                 :chat-type
                 :chat-id
                 :chat-name
                 :color
                 :invitation-admin])))

(re-frame/reg-sub
 :current-chat/one-to-one-chat?
 :<- [:chats/current-raw-chat]
 (fn [current-chat]
   (not (or (chat.events/group-chat? current-chat)
            (chat.events/public-chat? current-chat)))))

(re-frame/reg-sub
 :chats/photo-path
 :<- [:contacts/contacts]
 :<- [:profile/multiaccount]
 :<- [:mediaserver/port]
 (fn [[contacts {:keys [public-key] :as multiaccount} port] [_ id]]
   (let [contact (or (when (= id public-key)
                       multiaccount)
                     (get contacts id))]
     (if (nil? contact)
       (image-server/get-identicons-uri port id)
       (multiaccounts/displayed-photo contact)))))

(re-frame/reg-sub
 :chats/unread-messages-number
 :<- [:chats/home-list-chats]
 (fn [chats _]
   (reduce (fn [{:keys [public other]} {:keys [unviewed-messages-count public?] :as chat}]
             (if (or public? (chat.events/community-chat? chat))
               {:public (+ public unviewed-messages-count)
                :other  other}
               {:other  (+ other unviewed-messages-count)
                :public public}))
           {:public 0
            :other  0}
           chats)))

(re-frame/reg-sub
 :chats/current-chat-cooldown-enabled?
 :<- [:chats/current-chat]
 :<- [:chats/cooldown-enabled?]
 (fn [[{:keys [public?]} cooldown-enabled?]]
   (and public?
        cooldown-enabled?)))

(re-frame/reg-sub
 :chats/reply-message
 :<- [:chats/current-chat-input]
 (fn [{:keys [metadata]}]
   (:responding-to-message metadata)))

(re-frame/reg-sub
 :chats/edit-message
 :<- [:chats/current-chat-input]
 (fn [{:keys [metadata]}]
   (:editing-message metadata)))

(re-frame/reg-sub
 :chats/sending-contact-request
 :<- [:chats/current-chat-input]
 (fn [{:keys [metadata]}]
   (:sending-contact-request metadata)))

(re-frame/reg-sub
 :chats/timeline-sending-image
 :<- [:chats/timeline-chat-input]
 (fn [{:keys [metadata]}]
   (:sending-image metadata)))

(re-frame/reg-sub
 :chats/chat-toolbar
 :<- [:multiaccounts/login]
 :<- [:chats/sending-image]
 :<- [:mainnet?]
 :<- [:current-chat/one-to-one-chat?]
 :<- [:current-chat/metadata]
 :<- [:chats/reply-message]
 :<- [:chats/edit-message]
 :<- [:chats/sending-contact-request]
 (fn [[{:keys [processing]} sending-image mainnet? one-to-one-chat? {:keys [public?]} reply edit
       sending-contact-request]]
   (let [sending-image (seq sending-image)]
     {:send          (not processing)
      :stickers      (and (or config/stickers-test-enabled? mainnet?)
                          (not sending-image)
                          (not sending-contact-request)
                          (not reply))
      :image         (and (not reply)
                          (not edit)
                          (not sending-contact-request)
                          (not public?))
      :extensions    (and one-to-one-chat?
                          (or config/commands-enabled? mainnet?)
                          (not edit)
                          (not sending-contact-request)
                          (not reply))
      :audio         (and (not sending-image)
                          (not reply)
                          (not edit)
                          (not sending-contact-request)
                          (not public?))
      :sending-image sending-image})))

(re-frame/reg-sub
 :public-chat.new/topic-error-message
 :<- [:public-group-topic]
 (fn [topic]
   (when-not (or (empty? topic)
                 (db/valid-topic? topic))
     (i18n/label :topic-name-error))))

(defn filter-selected-contacts
  [selected-contacts contacts]
  (filter #(:added? (contacts %)) selected-contacts))

(re-frame/reg-sub
 :selected-contacts-count
 :<- [:group/selected-contacts]
 :<- [:contacts/contacts]
 (fn [[selected-contacts contacts]]
   (count (filter-selected-contacts selected-contacts contacts))))

(re-frame/reg-sub
 :selected-participants-count
 :<- [:selected-participants]
 (fn [selected-participants]
   (count selected-participants)))

(defn filter-contacts
  [selected-contacts active-contacts]
  (filter #(selected-contacts (:public-key %)) active-contacts))

(re-frame/reg-sub
 :selected-group-contacts
 :<- [:group/selected-contacts]
 :<- [:contacts/active]
 (fn [[selected-contacts active-contacts]]
   (filter-contacts selected-contacts active-contacts)))

(re-frame/reg-sub
 :group-chat/inviter-info
 (fn [[_ chat-id] _]
   [(re-frame/subscribe [:chat-by-id chat-id])
    (re-frame/subscribe [:multiaccount/public-key])])
 (fn [[chat my-public-key]]
   {:member?    (group-chats.db/member? my-public-key chat)
    :inviter-pk (group-chats.db/get-inviter-pk my-public-key chat)}))

(re-frame/reg-sub
 :group-chat/invitations-by-chat-id
 :<- [:group-chat/invitations]
 (fn [invitations [_ chat-id]]
   (filter #(= (:chat-id %) chat-id) (vals invitations))))

(re-frame/reg-sub
 :group-chat/pending-invitations-by-chat-id
 (fn [[_ chat-id] _]
   [(re-frame/subscribe [:group-chat/invitations-by-chat-id chat-id])])
 (fn [[invitations]]
   (filter #(= constants/invitation-state-requested (:state %)) invitations)))

(re-frame/reg-sub
 :group-chat/removed-from-current-chat?
 :<- [:chats/current-raw-chat]
 :<- [:multiaccount/public-key]
 (fn [[current-chat pk]]
   (group-chat/member-removed? current-chat pk)))

(re-frame/reg-sub
 :chats/mentionable-users
 :<- [:chats/current-chat]
 :<- [:contacts/blocked-set]
 :<- [:contacts/contacts]
 :<- [:multiaccount]
 :<- [:communities/current-community-members]
 (fn
   [[{:keys [users] :as chat}
     blocked
     all-contacts
     {:keys [public-key] :as current-multiaccount}
     community-members]]
   (let [mentionable-users (mentions/get-mentionable-users chat
                                                           all-contacts
                                                           current-multiaccount
                                                           community-members)
         members-left      (into #{} (filter #(group-chat/member-removed? chat %) (keys users)))]
     (apply dissoc mentionable-users (conj (concat blocked members-left) public-key)))))

(re-frame/reg-sub
 :chat/mention-suggestions
 :<- [:chats/current-chat-id]
 :<- [:chats/mention-suggestions]
 (fn [[chat-id mentions]]
   (take 15 (get mentions chat-id))))

(re-frame/reg-sub
 :chat/input-with-mentions
 :<- [:chats/current-chat-id]
 :<- [:chat/inputs-with-mentions]
 (fn [[chat-id cursor]]
   (get cursor chat-id)))
