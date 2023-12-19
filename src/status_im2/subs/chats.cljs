(ns status-im2.subs.chats
  (:require
    [clojure.string :as string]
    [legacy.status-im.group-chats.core :as group-chat]
    [legacy.status-im.group-chats.db :as group-chats.db]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.composer.constants :as composer.constants]
    [status-im2.contexts.chat.events :as chat.events]
    [status-im2.contexts.profile.utils :as profile.utils]))

(def memo-chats-stack-items (atom nil))

(re-frame/reg-sub
 :chats-stack-items
 :<- [:chats/home-list-chats]
 :<- [:view-id]
 :<- [:home-items-show-number]
 (fn [[chats view-id home-items-show-number]]
   (if (or (empty? @memo-chats-stack-items) (= view-id :chats-stack))
     (let [res (take home-items-show-number chats)]
       (reset! memo-chats-stack-items res)
       res)
     ;;we want to keep data unchanged so react doesn't change component when we leave screen
     @memo-chats-stack-items)))

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
     (group-by :categoryID (sort-by :position (map #(merge % (chat-cat (:chat-id %))) chats))))))

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
 :<- [:multiaccount/public-key]
 (fn [[chats active-chats my-public-key]]
   (reduce #(if-let [item (get chats %2)]
              (let [group-chat-member? (and (chat.events/group-chat? item)
                                            (group-chats.db/member? my-public-key item))]
                (conj %1
                      (assoc item
                             :group-chat-member?
                             group-chat-member?)))
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
 :chats/composer-height
 :<- [:chats/current-chat-input]
 :<- [:chats/link-previews-unfurled]
 (fn [[{:keys [input-content-height metadata]} link-previews]]
   (let [{:keys [responding-to-message editing-message sending-image]} metadata]
     (+ (max composer.constants/input-height input-content-height)
        (when responding-to-message
          composer.constants/reply-container-height)
        (when editing-message
          composer.constants/edit-container-height)
        (when (seq sending-image)
          composer.constants/images-container-height)
        (when (seq link-previews)
          composer.constants/links-container-height)
        composer.constants/bar-container-height
        composer.constants/actions-container-height))))

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
            (get-in community [:chats (subs (:chat-id current-chat) 68) :can-post?]))
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
 :<- [:profile/profile-with-image]
 (fn [[contacts {:keys [public-key] :as multiaccount}] [_ id]]
   (let [contact (or (when (= id public-key) multiaccount) (get contacts id))]
     (profile.utils/photo contact))))

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
 :chats/sending-audio
 :<- [:chats/current-chat-id]
 :<- [:chat/inputs]
 (fn [[chat-id inputs]]
   (get-in inputs [chat-id :audio])))

(re-frame/reg-sub
 :chats/timeline-sending-image
 :<- [:chats/timeline-chat-input]
 (fn [{:keys [metadata]}]
   (:sending-image metadata)))

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

(re-frame/reg-sub
 :chats/link-previews-unfurled
 :<- [:chat/link-previews]
 (fn [previews]
   (get previews :unfurled)))

(re-frame/reg-sub
 :chats/link-previews?
 :<- [:chats/link-previews-unfurled]
 (fn [previews]
   (boolean (seq previews))))

(re-frame/reg-sub
 :chat/check-channel-muted?
 (fn [[_ community-id channel-id]]
   [(re-frame/subscribe [:chats/chat (str community-id channel-id)])])
 (fn [[chat]]
   (:muted? chat)))

(re-frame/reg-sub
 :camera-roll/total-photos-count-android
 (fn [{:keys [camera-roll/albums]}]
   (->> albums
        :my-albums
        (reduce
         (fn [total-album-count current-album]
           (+ total-album-count (:count current-album)))
         0))))

(re-frame/reg-sub
 :camera-roll/total-photos-count-ios
 (fn [{:keys [camera-roll/ios-images-count]}]
   ios-images-count))
