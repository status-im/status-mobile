(ns status-im.subs.chats
  (:require
    [clojure.string :as string]
    [legacy.status-im.group-chats.db :as group-chats.db]
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
    [status-im.contexts.chat.events :as chat.events]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.subs.contact.utils :as contact.utils]))

(def memo-chats-stack-items (atom nil))

(re-frame/reg-sub
 :chats/chats-stack-items
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
 :chats/chat-by-id
 :<- [:chats/chats]
 (fn [chats [_ chat-id]]
   (get chats chat-id)))

(re-frame/reg-sub
 :chats/chat-members-by-id
 :<- [:chats/chats]
 (fn [chats [_ chat-id]]
   (get-in chats [chat-id :members])))

(re-frame/reg-sub
 :chats/muted
 (fn [[_ chat-id] _]
   (re-frame/subscribe [:chats/chat-by-id chat-id]))
 (fn [{:keys [muted]}]
   muted))

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
 (fn [[{:keys [group-chat chat-id chat-name name] :as current-chat} my-public-key community
       blocked-users-set contacts
       inputs]]
   (when current-chat
     (cond-> current-chat

       (and (chat.events/community-chat? current-chat)
            (get-in community [:chats (subs (:chat-id current-chat) 68) :can-post?]))
       (assoc :able-to-send-message? true)


       (and (chat.events/group-chat? current-chat)
            (group-chats.db/member? my-public-key current-chat))
       (assoc :able-to-send-message? true
              :member?               true)

       (not chat-name)
       (assoc :chat-name name)

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
 :chats/able-to-send-message?
 :<- [:chats/current-chat]
 (fn [current-chat]
   (get current-chat :able-to-send-message?)))

(re-frame/reg-sub
 :chats/chat-type
 :<- [:chats/current-chat]
 (fn [current-chat]
   (condp apply [current-chat]
     chat.events/community-chat? :community-chat
     chat.events/group-chat?     :group-chat
     chat.events/public-chat?    :public-chat
     :chat)))

(re-frame/reg-sub :chats/current-chat-chat-view
 :<- [:chats/current-chat]
 (fn [chat]
   {:able-to-send-message? (:able-to-send-message? chat)
    :admins                (:admins chat)
    :chat-id               (:chat-id chat)
    :chat-name             (:chat-name chat)
    :chat-type             (:chat-type chat)
    :color                 (:color chat)
    :community-id          (:community-id chat)
    :contact-request-state (:contact-request-state chat)
    :description           (:description chat)
    :emoji                 (:emoji chat)
    :empty-chat?           (not (:last-message chat))
    :group-chat            (:group-chat chat)
    :invitation-admin      (:invitation-admin chat)
    :name                  (:name chat)
    :public?               (:public? chat)
    :synced-from           (:synced-from chat)
    :synced-to             (:synced-to chat)}))

(re-frame/reg-sub
 :chats/current-chat-exist?
 :<- [:chats/current-chat-chat-view]
 (fn [current-chat]
   (boolean (:chat-id current-chat))))

(re-frame/reg-sub
 :chats/current-chat-color
 :<- [:chats/current-raw-chat]
 (fn [current-chat]
   (:color current-chat)))

(re-frame/reg-sub
 :chats/community-channel-ui-details-by-id
 :<- [:chats/chats]
 (fn [chats [_ chat-id]]
   (select-keys
    (get chats chat-id)
    [:chat-name
     :color
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
         community-member? (get current-community :is-member? false)

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
      :community-member?                community-member?
      :message-pin-enabled              message-pin-enabled
      :can-delete-message-for-everyone? can-delete-message-for-everyone?})))

(re-frame/reg-sub
 :chats/current-chat-one-to-one?
 :<- [:chats/current-raw-chat]
 (fn [{:keys [chat-type]}]
   (= chat-type constants/one-to-one-chat-type)))

(re-frame/reg-sub
 :chats/photo-path
 :<- [:contacts/contacts]
 :<- [:profile/profile-with-image]
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 :<- [:theme]
 (fn [[contacts {:keys [public-key] :as multiaccount} port font-file theme] [_ id]]
   (let [contact (or (when (= id public-key) multiaccount)
                     (get contacts id)
                     (contact.utils/replace-contact-image-uri
                      {:contact    {:public-key          id
                                    :customization-color constants/profile-default-color}
                       :port       port
                       :public-key id
                       :font-file  font-file
                       :theme      theme}))]
     (profile.utils/photo contact))))

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
 :chat/mention-suggestions
 :<- [:chats/current-chat-id]
 :<- [:chats/mention-suggestions]
 (fn [[chat-id mentions]]
   (take 15 (get mentions chat-id))))

(re-frame/reg-sub
 :chats/link-previews-unfurled
 :<- [:chat/link-previews]
 (fn [previews]
   (get previews :unfurled)))

(re-frame/reg-sub
 :chats/status-link-previews-unfurled
 :<- [:chat/status-link-previews]
 (fn [previews]
   (:unfurled previews)))

(re-frame/reg-sub
 :chats/link-previews?
 :<- [:chats/link-previews-unfurled]
 (fn [previews]
   (boolean (seq previews))))

(re-frame/reg-sub
 :chats/status-link-previews?
 :<- [:chats/status-link-previews-unfurled]
 (fn [status-link-previews]
   (boolean (seq status-link-previews))))

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

(re-frame/reg-sub
 :chats/group-chat-image
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 :->
 :image)
