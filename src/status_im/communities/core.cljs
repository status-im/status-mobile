(ns status-im.communities.core
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.async-storage.core :as async-storage]
            [status-im2.common.bottom-sheet.events :as bottom-sheet]
            [status-im2.constants :as constants]
            [status-im.ui.components.emoji-thumbnail.styles :as emoji-thumbnail-styles]
            [utils.re-frame :as rf]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im2.contexts.activity-center.events :as activity-center]
            [status-im2.common.toasts.events :as toasts]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]))

(def crop-size 1000)

(defn universal-link
  [community-id]
  (str (:external universal-links/domains)
       "/c/"
       community-id))

(def featured
  [{:name "Status"
    :id   constants/status-community-id}])

(defn <-request-to-join-community-rpc
  [r]
  (set/rename-keys r
                   {:communityId :community-id
                    :publicKey   :public-key
                    :chatId      :chat-id}))

(defn <-requests-to-join-community-rpc
  [requests]
  (reduce (fn [acc r]
            (assoc acc (:id r) (<-request-to-join-community-rpc r)))
          {}
          requests))

(defn <-chats-rpc
  [chats]
  (reduce-kv (fn [acc k v]
               (assoc acc
                      (name k)
                      (-> v
                          (assoc :can-post? (:canPost v))
                          (dissoc :canPost)
                          (update :members walk/stringify-keys))))
             {}
             chats))

(defn <-categories-rpc
  [categ]
  (reduce-kv (fn [acc k v]
               (assoc acc
                      (name k)
                      v))
             {}
             categ))

(defn <-rpc
  [c]
  (-> c
      (set/rename-keys {:canRequestAccess            :can-request-access?
                        :canManageUsers              :can-manage-users?
                        :canDeleteMessageForEveryone :can-delete-message-for-everyone?
                        :canJoin                     :can-join?
                        :requestedToJoinAt           :requested-to-join-at
                        :isMember                    :is-member?})
      (update :members walk/stringify-keys)
      (update :chats <-chats-rpc)
      (update :categories <-categories-rpc)))

(defn fetch-community-id-input
  [{:keys [db]}]
  (:communities/community-id-input db))

(rf/defn handle-request-to-join
  [{:keys [db]} r]
  (let [{:keys [id community-id] :as request} (<-request-to-join-community-rpc r)]
    {:db (assoc-in db [:communities/requests-to-join community-id id] request)}))

(rf/defn handle-removed-chats
  [{:keys [db]} chat-ids]
  {:db (reduce (fn [db chat-id]
                 (update db :chats dissoc chat-id))
               db
               chat-ids)})

(rf/defn handle-community
  [{:keys [db]} {:keys [id] :as community}]
  (when id
    {:db (assoc-in db [:communities id] (<-rpc community))}))

(rf/defn handle-communities
  {:events [::fetched]}
  [{:keys [db]} communities]
  {:db (reduce (fn [db {:keys [id] :as community}]
                 (assoc-in db [:communities id] (<-rpc community)))
               db
               communities)})

(rf/defn handle-response
  [_ response-js]
  {:dispatch [:sanitize-messages-and-process-response response-js]})

(rf/defn left
  {:events [::left]}
  [cofx response-js]
  (let [community-name (aget response-js "communities" 0 "name")]
    (rf/merge cofx
              (handle-response cofx response-js)
              (toasts/upsert {:icon       :placeholder
                              :icon-color (:positive-01 @colors/theme)
                              :text       (i18n/label :t/left-community {:community community-name})})
              (navigation/navigate-back)
              (activity-center/notifications-fetch-unread-count))))

(rf/defn joined
  {:events [::joined ::requested-to-join]}
  [cofx response-js]
  (let [[event-name _] (:event cofx)
        community-name (aget response-js "communities" 0 "name")]
    (rf/merge cofx
              (handle-response cofx response-js)
              (toasts/upsert {:icon       :placeholder
                              :icon-color (:positive-01 @colors/theme)
                              :text       (i18n/label (if (= event-name ::joined)
                                                        :t/joined-community
                                                        :t/requested-to-join-community)
                                                      {:community community-name})}))))

(rf/defn export
  {:events [::export-pressed]}
  [cofx community-id]
  {:json-rpc/call [{:method     "wakuext_exportCommunity"
                    :params     [community-id]
                    :on-success #(re-frame/dispatch [:show-popover
                                                     {:view          :export-community
                                                      :community-key %}])
                    :on-error   #(do
                                   (log/error "failed to export community" community-id %)
                                   (re-frame/dispatch [::failed-to-export %]))}]})

(rf/defn import-community
  {:events [::import]}
  [cofx community-key]
  {:json-rpc/call [{:method      "wakuext_importCommunity"
                    :params      [community-key]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::community-imported %])
                    :on-error    #(do
                                    (log/error "failed to import community" %)
                                    (re-frame/dispatch [::failed-to-import %]))}]})

(rf/defn join
  {:events [:communities/join]}
  [cofx community-id]
  {:json-rpc/call [{:method      "wakuext_joinCommunity"
                    :params      [community-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::joined %])
                    :on-error    #(do
                                    (log/error "failed to join community" community-id %)
                                    (re-frame/dispatch [::failed-to-join %]))}]})

(rf/defn request-to-join
  {:events [:communities/request-to-join]}
  [cofx community-id]
  {:json-rpc/call [{:method      "wakuext_requestToJoinCommunity"
                    :params      [{:communityId community-id}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::requested-to-join %])
                    :on-error    #(do
                                    (log/error "failed to request to join community" community-id %)
                                    (re-frame/dispatch [::failed-to-request-to-join %]))}]})

(rf/defn leave
  {:events [:communities/leave]}
  [{:keys [db]} community-id]
  (let [community-chat-ids (map #(str community-id %)
                                (keys (get-in db [:communities community-id :chats])))]
    {:clear-message-notifications [community-chat-ids
                                   (get-in db [:multiaccount :remote-push-notifications-enabled?])]
     :dispatch                    [:shell/close-switcher-card community-id]
     :json-rpc/call               [{:method      "wakuext_leaveCommunity"
                                    :params      [community-id]
                                    :js-response true
                                    :on-success  #(re-frame/dispatch [::left %])
                                    :on-error    #(do
                                                    (log/error "failed to leave community"
                                                               community-id
                                                               %)
                                                    (re-frame/dispatch [::failed-to-leave %]))}]}))

(rf/defn status-tag-pressed
  {:events [:communities/status-tag-pressed]}
  [{:keys [db]} community-id literal]
  (let [current-chat-id (:current-chat-id db)
        {:keys [id]}    (some #(when (= (:name %) literal) %)
                              (vals (get-in db [:communities community-id :chats])))]
    (cond-> {}
      (not= current-chat-id (str community-id id)) (assoc :dispatch
                                                          [:chat/navigate-to-chat
                                                           (str community-id id)]))))

(rf/defn fetch
  [_]
  {:json-rpc/call [{:method     "wakuext_communities"
                    :params     []
                    :on-success #(re-frame/dispatch [::fetched %])
                    :on-error   #(do
                                   (log/error "failed to fetch communities" %)
                                   (re-frame/dispatch [::failed-to-fetch %]))}]})

(rf/defn chat-created
  {:events [::chat-created]}
  [_ community-id user-pk]
  {:json-rpc/call [{:method "wakuext_sendChatMessage"
                    :params [{:chatId      user-pk
                              :text        "Upgrade here to see an invitation to community"
                              :communityId community-id
                              :contentType constants/content-type-community}]
                    :js-response true
                    :on-success
                    #(re-frame/dispatch [:transport/message-sent %])
                    :on-error
                    #(log/error "failed to send a message" %)}]})

(rf/defn invite-users
  {:events [::invite-people-confirmation-pressed]}
  [cofx user-pk contacts]
  (let [community-id (fetch-community-id-input cofx)
        pks          (if (seq user-pk)
                       (conj contacts user-pk)
                       contacts)]
    (when (seq pks)
      {:json-rpc/call [{:method      "wakuext_inviteUsersToCommunity"
                        :params      [{:communityId community-id
                                       :users       pks}]
                        :js-response true
                        :on-success  #(re-frame/dispatch [::people-invited %])
                        :on-error    #(do
                                        (log/error "failed to invite-user community" %)
                                        (re-frame/dispatch [::failed-to-invite-people %]))}]})))
(rf/defn share-community
  {:events [::share-community-confirmation-pressed]}
  [cofx user-pk contacts]
  (let [community-id (fetch-community-id-input cofx)
        pks          (if (seq user-pk)
                       (conj contacts user-pk)
                       contacts)]
    (when (seq pks)
      {:json-rpc/call [{:method      "wakuext_shareCommunity"
                        :params      [{:communityId community-id
                                       :users       pks}]
                        :js-response true
                        :on-success  #(re-frame/dispatch [::people-invited %])
                        :on-error    #(do
                                        (log/error "failed to invite-user community" %)
                                        (re-frame/dispatch [::failed-to-share-community %]))}]})))

(rf/defn create
  {:events [::create-confirmation-pressed]}
  [{:keys [db]}]
  (let [{:keys [name description membership image]} (get db :communities/create)]
    (let [params {:name        name
                  :description description
                  :membership  membership
                  :color       (rand-nth colors/chat-colors)
                  :image       (string/replace-first (str image) #"file://" "")
                  :imageAx     0
                  :imageAy     0
                  :imageBx     crop-size
                  :imageBy     crop-size}]

      {:json-rpc/call [{:method      "wakuext_createCommunity"
                        :params      [params]
                        :js-response true
                        :on-success  #(re-frame/dispatch [::community-created %])
                        :on-error    #(do
                                        (log/error "failed to create community" %)
                                        (re-frame/dispatch [::failed-to-create-community %]))}]})))

(rf/defn edit
  {:events [::edit-confirmation-pressed]}
  [{:keys [db]}]
  (let [{:keys [id name description membership new-image color]} (get db :communities/create)]
    {:json-rpc/call [{:method     "wakuext_editCommunity"
                      :params     [{:communityID id
                                    :name        name
                                    :description description
                                    :color       color
                                    :image       (string/replace-first (str new-image) #"file://" "")
                                    :imageAx     0
                                    :imageAy     0
                                    :imageBx     crop-size
                                    :imageBy     crop-size
                                    :membership  membership}]
                      :on-success #(re-frame/dispatch [::community-edited %])
                      :on-error   #(do
                                     (log/error "failed to edit community" %)
                                     (re-frame/dispatch [::failed-to-edit-community %]))}]}))

(rf/defn create-channel
  {:events [::create-channel-confirmation-pressed]}
  [{:keys [db] :as cofx}]
  (let [community-id                           (fetch-community-id-input cofx)
        {:keys [name description color emoji]} (get db :communities/create-channel)]
    {:json-rpc/call [{:method      "wakuext_createCommunityChat"
                      :params      [community-id
                                    {:identity    {:display_name name
                                                   :description  description
                                                   :color        color
                                                   :emoji        emoji}
                                     :permissions {:access
                                                   constants/community-channel-access-no-membership}}]
                      :js-response true
                      :on-success  #(re-frame/dispatch [::community-channel-created %])
                      :on-error    #(do
                                      (log/error "failed to create community channel" %)
                                      (re-frame/dispatch [::failed-to-create-community-channel %]))}]}))

(def community-chat-id-length 68)

(defn to-community-chat-id
  [chat-id]
  (subs chat-id community-chat-id-length))

(rf/defn edit-channel
  {:events [::edit-channel-confirmation-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [name description color community-id emoji edit-channel-id category-id position]}
        (get db :communities/create-channel)]
    {:json-rpc/call [{:method      "wakuext_editCommunityChat"
                      :params      [community-id
                                    edit-channel-id
                                    {:identity    {:display_name name
                                                   :description  description
                                                   :color        color
                                                   :emoji        emoji}
                                     :category_id category-id
                                     :position    position
                                     :permissions {:access
                                                   constants/community-channel-access-no-membership}}]
                      :js-response true
                      :on-success  #(re-frame/dispatch [::community-channel-edited %])
                      :on-error    #(do
                                      (log/error "failed to edit community channel" %)
                                      (re-frame/dispatch [::failed-to-edit-community-channel %]))}]}))

(defn require-membership?
  [permissions]
  (not= constants/community-no-membership-access (:access permissions)))

(defn can-post?
  [community _ local-chat-id]
  (let [chat-id (to-community-chat-id local-chat-id)]
    (get-in community [:chats chat-id :can-post?])))

(rf/defn reset-community-id-input
  [{:keys [db]} id]
  {:db (assoc db :communities/community-id-input id)})

(rf/defn reset-channel-info
  [{:keys [db]}]
  {:db (assoc db :communities/create-channel {})})

(rf/defn invite-people-pressed
  {:events [:communities/invite-people-pressed]}
  [cofx id]
  (rf/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/hide-bottom-sheet)
            (navigation/open-modal :invite-people-community {:invite? true})))

(rf/defn share-community-pressed
  {:events [:communities/share-community-pressed]}
  [cofx id]
  (rf/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/hide-bottom-sheet)
            (navigation/open-modal :invite-people-community {})))

(rf/defn create-channel-pressed
  {:events [::create-channel-pressed]}
  [{:keys [db] :as cofx} id]
  (rf/merge cofx
            (reset-community-id-input id)
            (reset-channel-info)
            (rf/dispatch [::create-channel-fields
                          (rand-nth emoji-thumbnail-styles/emoji-picker-default-thumbnails)])
            (navigation/open-modal :create-community-channel {:community-id id})))

(rf/defn edit-channel-pressed
  {:events [::edit-channel-pressed]}
  [{:keys [db] :as cofx} community-id chat-name description color emoji chat-id category-id position]
  (let [{:keys [color emoji]} (if (string/blank? emoji)
                                (rand-nth emoji-thumbnail-styles/emoji-picker-default-thumbnails)
                                {:color color :emoji emoji})]
    (rf/merge cofx
              {:db (assoc db
                          :communities/create-channel
                          {:name            chat-name
                           :description     description
                           :color           color
                           :community-id    community-id
                           :emoji           emoji
                           :edit-channel-id chat-id
                           :category-id     category-id
                           :position        position})}
              (navigation/open-modal :edit-community-channel nil))))

(rf/defn community-created
  {:events [::community-created]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(rf/defn community-edited
  {:events [::community-edited]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(rf/defn open-create-community
  {:events [::open-create-community]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db :communities/create {:membership constants/community-no-membership-access})}
            (navigation/navigate-to :community-create nil)))

(rf/defn open-edit-community
  {:events [::open-edit-community]}
  [{:keys [db] :as cofx} id]
  (let [{:keys [name description images permissions color]} (get-in db [:communities id])
        {:keys [access]}                                    permissions]
    (rf/merge cofx
              {:db (assoc db
                          :communities/create
                          {:id          id
                           :name        name
                           :description description
                           :image       (get-in images [:large :uri])
                           :membership  access
                           :color       color
                           :editing?    true})}
              (navigation/navigate-to :community-edit nil))))

(rf/defn community-imported
  {:events [::community-imported]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(rf/defn people-invited
  {:events [::people-invited]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(rf/defn community-channel-created
  {:events [::community-channel-created]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(rf/defn community-channel-edited
  {:events [::community-channel-edited]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(rf/defn create-field
  {:events [::create-field]}
  [{:keys [db]} field value]
  {:db (assoc-in db [:communities/create field] value)})

(rf/defn remove-field
  {:events [::remove-field]}
  [{:keys [db]} field]
  {:db (update-in db [:communities/create] dissoc field)})

(rf/defn create-channel-field
  {:events [::create-channel-field]}
  [{:keys [db]} field value]
  {:db (assoc-in db [:communities/create-channel field] value)})

(rf/defn create-channel-fields
  {:events [::create-channel-fields]}
  [{:keys [db]} field-values]
  {:db (update-in db [:communities/create-channel] merge field-values)})

(rf/defn member-banned
  {:events [::member-banned]}
  [cofx response-js]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response-js)
            (activity-center/notifications-fetch-unread-count)))

(rf/defn member-ban
  {:events [::member-ban]}
  [cofx community-id public-key]
  {:json-rpc/call [{:method      "wakuext_banUserFromCommunity"
                    :params      [{:communityId community-id
                                   :user        public-key}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::member-banned %])
                    :on-error    #(log/error "failed to ban user from community"
                                             community-id
                                             public-key
                                             %)}]})

(rf/defn member-kicked
  {:events [::member-kicked]}
  [cofx response-js]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response-js)))

(rf/defn member-kick
  {:events [::member-kick]}
  [cofx community-id public-key]
  {:json-rpc/call [{:method      "wakuext_removeUserFromCommunity"
                    :params      [community-id public-key]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::member-kicked %])
                    :on-error    #(log/error "failed to remove user from community"
                                             community-id
                                             public-key
                                             %)}]})

(rf/defn delete-community
  {:events [::delete-community]}
  [cofx community-id]
  (log/error "Community delete is not yet implemented"))

(rf/defn requests-to-join-fetched
  {:events [::requests-to-join-fetched]}
  [{:keys [db]} community-id requests]
  {:db (assoc-in db
        [:communities/requests-to-join community-id]
        (<-requests-to-join-community-rpc requests))})

(rf/defn fetch-requests-to-join
  {:events [::fetch-requests-to-join]}
  [cofx community-id]
  {:json-rpc/call [{:method     "wakuext_pendingRequestsToJoinForCommunity"
                    :params     [community-id]
                    :on-success #(re-frame/dispatch [::requests-to-join-fetched community-id %])
                    :on-error   #(log/error "failed to fetch requests-to-join" community-id %)}]})

(defn fetch-requests-to-join!
  [community-id]
  (re-frame/dispatch [::fetch-requests-to-join community-id]))

(rf/defn request-to-join-accepted
  {:events [::request-to-join-accepted]}
  [{:keys [db] :as cofx} community-id request-id response-js]
  (rf/merge
   cofx
   {:db         (update-in db [:communities/requests-to-join community-id] dissoc request-id)
    :dispatch-n [[:sanitize-messages-and-process-response response-js]
                 [:activity-center.notifications/mark-as-read request-id]]}))

(rf/defn request-to-join-declined
  {:events [::request-to-join-declined]}
  [{:keys [db] :as cofx} community-id request-id response-js]
  (rf/merge
   cofx
   {:db         (update-in db [:communities/requests-to-join community-id] dissoc request-id)
    :dispatch-n [[:sanitize-messages-and-process-response response-js]
                 [:activity-center.notifications/mark-as-read request-id]]}))

(rf/defn accept-request-to-join-pressed
  {:events [:communities.ui/accept-request-to-join-pressed]}
  [cofx community-id request-id]
  {:json-rpc/call [{:method      "wakuext_acceptRequestToJoinCommunity"
                    :params      [{:id request-id}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::request-to-join-accepted community-id request-id
                                                      %])
                    :on-error    #(log/error "failed to accept requests-to-join"
                                             community-id
                                             request-id
                                             %)}]})

(rf/defn decline-request-to-join-pressed
  {:events [:communities.ui/decline-request-to-join-pressed]}
  [cofx community-id request-id]
  {:json-rpc/call [{:method      "wakuext_declineRequestToJoinCommunity"
                    :params      [{:id request-id}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [::request-to-join-declined community-id request-id
                                                      %])
                    :on-error    #(log/error "failed to decline requests-to-join"
                                             community-id
                                             request-id)}]})

(rf/defn switch-communities-enabled
  {:events [:multiaccounts.ui/switch-communities-enabled]}
  [{:keys [db]} enabled?]
  {::async-storage/set! {:communities-enabled? enabled?}
   :db                  (assoc db :communities/enabled? enabled?)})

(rf/defn create-category
  {:events [::create-category-confirmation-pressed]}
  [_ community-id category-title chat-ids]
  {:json-rpc/call [{:method      "wakuext_createCommunityCategory"
                    :params      [{:communityId  community-id
                                   :categoryName category-title
                                   :chatIds      (map #(string/replace % community-id "") chat-ids)}]
                    :js-response true
                    :on-success  #(do
                                    (re-frame/dispatch [:navigate-back])
                                    (re-frame/dispatch [:sanitize-messages-and-process-response %]))
                    :on-error    #(log/error "failed to create community category" %)}]})

(rf/defn remove-chat-from-category
  {:events [:remove-chat-from-community-category]}
  [{:keys [db]} community-id id categoryID]
  (let [category       (get-in db [:communities community-id :categories categoryID])
        category-chats (map :id
                            (filter #(and (= (:categoryID %) categoryID) (not= id (:id %)))
                                    (vals (get-in db [:communities community-id :chats]))))]
    {:json-rpc/call [{:method      "wakuext_editCommunityCategory"
                      :params      [{:communityId  community-id
                                     :categoryId   categoryID
                                     :categoryName (:name category)
                                     :chatIds      category-chats}]
                      :js-response true
                      :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                      :on-error    #(log/error "failed to remove chat from community" %)}]}))

(rf/defn delete-community-chat
  {:events [:delete-community-chat]}
  [_ community-id chat-id]
  {:json-rpc/call [{:method      "wakuext_deleteCommunityChat"
                    :params      [community-id chat-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to delete community chat" %)}]})

(rf/defn delete-category
  {:events [:delete-community-category]}
  [_ community-id category-id]
  {:json-rpc/call [{:method      "wakuext_deleteCommunityCategory"
                    :params      [{:communityId community-id
                                   :categoryId  category-id}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to delete community category" %)}]})

(rf/defn change-category
  {:events [::change-category-confirmation-pressed]}
  [cofx community-id category-id {:keys [id position categoryID]}]
  (if (not (string/blank? category-id))
    {:json-rpc/call [{:method      "wakuext_reorderCommunityChat"
                      :params      [{:communityId community-id
                                     :categoryId  category-id
                                     :chatId      id
                                     :position    position}]
                      :js-response true
                      :on-success  #(do
                                      (re-frame/dispatch [:navigate-back])
                                      (re-frame/dispatch [:sanitize-messages-and-process-response %]))
                      :on-error    #(log/error "failed to change community category" %)}]}
    (rf/merge cofx
              (navigation/navigate-back)
              (remove-chat-from-category community-id id categoryID))))

(rf/defn reorder-category-chat
  {:events [::reorder-community-category-chat]}
  [_ community-id category-id chat-id new-position]
  {:json-rpc/call [{:method      "wakuext_reorderCommunityChat"
                    :params      [{:communityId community-id
                                   :categoryId  category-id
                                   :chatId      chat-id
                                   :position    new-position}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to reorder community category chat" %)}]})

(rf/defn reorder-category
  {:events [::reorder-community-category]}
  [_ community-id category-id new-position]
  {:json-rpc/call [{:method      "wakuext_reorderCommunityCategories"
                    :params      [{:communityId community-id
                                   :categoryId  category-id
                                   :position    new-position}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to reorder community category" %)}]})

(defn category-hash
  [public-key community-id category-id]
  (hash (str public-key community-id category-id)))

(rf/defn store-category-state
  {:events [::store-category-state]}
  [{:keys [db]} community-id category-id state-open?]
  (let [public-key (get-in db [:multiaccount :public-key])
        hash       (category-hash public-key community-id category-id)]
    {::async-storage/set! {hash state-open?}}))

(rf/defn update-category-states-in-db
  {:events [::category-states-loaded]}
  [{:keys [db]} community-id hashes states]
  (let [public-key     (get-in db [:multiaccount :public-key])
        categories-old (get-in db [:communities community-id :categories])
        categories     (reduce (fn [acc [category-id category]]
                                 (let [hash             (get hashes category-id)
                                       state            (get states hash)
                                       category-updated (assoc category :state state)]
                                   (assoc acc category-id category-updated)))
                               {}
                               categories-old)]
    {:db (update-in db [:communities community-id :categories] merge categories)}))

(rf/defn load-category-states
  {:events [:communities/load-category-states]}
  [{:keys [db]} community-id]
  (let [public-key            (get-in db [:multiaccount :public-key])
        categories            (get-in db [:communities community-id :categories])
        {:keys [keys hashes]} (reduce (fn [acc category]
                                        (let [category-id (get category 0)
                                              hash        (category-hash
                                                           public-key
                                                           community-id
                                                           category-id)]
                                          (-> acc
                                              (assoc-in [:hashes category-id] hash)
                                              (update :keys #(conj % hash)))))
                                      {}
                                      categories)]
    {::async-storage/get {:keys keys
                          :cb   #(re-frame/dispatch
                                  [::category-states-loaded community-id hashes %])}}))

(rf/defn navigate-to-community
  {:events [:communities/navigate-to-community]}
  [cofx community-id]
  (rf/merge cofx
            (navigation/pop-to-root-tab :shell-stack)
            (navigation/navigate-to-nav2 :community community-id true)))

(rf/defn member-role-updated
  {:events [:community.member/role-updated]}
  [cofx response-js]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response-js)))

(rf/defn add-role-to-member
  {:events [:community.member/add-role]}
  [cofx community-id public-key role-id]
  {:json-rpc/call [{:method     "wakuext_addRoleToMember"
                    :params     [{:communityId community-id
                                  :user        public-key
                                  :role        role-id}]
                    :on-success #(re-frame/dispatch [:community.member/role-updated %])
                    :on-error   #(log/error "failed to add role to member"
                                            {:error        %
                                             :community-id community-id
                                             :public-key   public-key
                                             :role-id      role-id})}]})

(rf/defn remove-role-from-member
  {:events [:community.member/remove-role]}
  [_ community-id public-key role-id]
  {:json-rpc/call [{:method "wakuext_removeRoleFromMember"
                    :params [{:communityId community-id
                              :user        public-key
                              :role        role-id}]}
                   :on-success #(re-frame/dispatch [:community.member/role-updated %])
                   :on-error
                   #(log/error "failed to remove role from member"
                               {:error        %
                                :community-id community-id
                                :public-key   public-key
                                :role-id      role-id})]})
