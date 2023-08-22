(ns status-im.communities.core
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as quo2.colors]
            [re-frame.core :as re-frame]
            [status-im.utils.types :as types]
            [status-im.async-storage.core :as async-storage]
            [status-im.ui.components.emoji-thumbnail.styles :as emoji-thumbnail-styles]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.bottom-sheet.events :as bottom-sheet]
            [status-im2.common.toasts.events :as toasts]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.activity-center.events :as activity-center]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [status-im2.common.muting.helpers :refer [format-mute-till]]
            [status-im2.contexts.chat.events :as chat.events]))

(def crop-size 1000)

(defn universal-link
  [community-id]
  (str (:external universal-links/domains)
       "/c/"
       community-id))

(defn <-request-to-join-community-rpc
  [r]
  (set/rename-keys r
                   {:communityId :community-id
                    :publicKey   :public-key
                    :chatId      :chat-id}))

(defn <-requests-to-join-community-rpc
  [requests key-fn]
  (reduce (fn [acc r]
            (assoc acc (key-fn r) (<-request-to-join-community-rpc r)))
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
                        :isMember                    :is-member?
                        :adminSettings               :admin-settings
                        :tokenPermissions            :token-permissions
                        :communityTokensMetadata     :tokens-metadata
                        :muteTill                    :muted-till})
      (update :admin-settings
              set/rename-keys
              {:pinMessageAllMembersEnabled :pin-message-all-members-enabled?})
      (update :members walk/stringify-keys)
      (update :chats <-chats-rpc)
      (update :categories <-categories-rpc)
      (assoc :token-images
             (reduce (fn [acc {sym :symbol image :image}]
                       (assoc acc sym image))
                     {}
                     (:communityTokensMetadata c)))))

(defn- fetch-community-id-input
  [{:keys [db]}]
  (:communities/community-id-input db))

(defn- handle-my-request
  [db {:keys [community-id state deleted] :as request}]
  (if (and (= constants/community-request-to-join-state-pending state) (not deleted))
    (assoc-in db [:communities/my-pending-requests-to-join community-id] request)
    (update-in db [:communities/my-pending-requests-to-join] dissoc community-id)))

(defn handle-admin-request
  [db {:keys [id community-id deleted] :as request}]
  (if deleted
    (update-in db [:communities/requests-to-join community-id] dissoc id)
    (assoc-in db [:communities/requests-to-join community-id id] request)))

(rf/defn handle-requests-to-join
  [{:keys [db]} requests]
  (let [my-public-key (get-in db [:profile/profile :public-key])]
    {:db (reduce (fn [db {:keys [public-key] :as request}]
                   (let [my-request? (= my-public-key public-key)]
                     (if my-request?
                       (handle-my-request db request)
                       (handle-admin-request db request))))
                 db
                 requests)}))

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

(rf/defn handle-my-pending-requests-to-join
  {:events [:communities/fetched-my-communities-requests-to-join]}
  [{:keys [db]} my-requests]
  {:db (assoc db
              :communities/my-pending-requests-to-join
              (<-requests-to-join-community-rpc (types/js->clj my-requests)
                                                :communityId))})

(rf/defn handle-response
  [_ response-js]
  {:dispatch [:sanitize-messages-and-process-response response-js]})

(rf/defn left
  {:events [::left]}
  [cofx response-js]
  (let [community-name (aget response-js "communities" 0 "name")]
    (rf/merge cofx
              (handle-response cofx response-js)
              (toasts/upsert {:icon       :correct
                              :icon-color (:positive-01 @colors/theme)
                              :text       (i18n/label :t/left-community {:community community-name})})
              (navigation/navigate-back)
              (activity-center/notifications-fetch-unread-count))))

(rf/defn joined
  {:events [::joined ::requested-to-join]}
  [cofx response-js]
  (let [[event-name _] (:event cofx)
        community-name (aget response-js "communities" 0 "name")]
    (js/console.log "event-name")
    (rf/merge cofx
              (handle-response cofx response-js)
              (toasts/upsert {:icon       :correct
                              :icon-color (:positive-01 @colors/theme)
                              :text       (i18n/label (if (= event-name ::joined)
                                                        :t/joined-community
                                                        :t/requested-to-join-community)
                                                      {:community community-name})}))))

(rf/defn requested-to-join
  {:events [:communities/requested-to-join]}
  [cofx response-js]
  (let [community-name (aget response-js "communities" 0 "name")]
    (rf/merge cofx
              (handle-response cofx response-js)
              (navigation/hide-bottom-sheet)
              (toasts/upsert {:icon       :correct
                              :icon-color (:positive-01 @colors/theme)
                              :text       (i18n/label
                                           :t/requested-to-join-community
                                           {:community community-name})}))))

(rf/defn cancelled-requested-to-join
  {:events [:communities/cancelled-request-to-join]}
  [cofx response-js]
  (rf/merge cofx
            (handle-response cofx response-js)
            (toasts/upsert {:icon       :correct
                            :icon-color (:positive-01 @colors/theme)
                            :text       (i18n/label
                                         :t/you-canceled-the-request)})))

(rf/defn export
  {:events [::export-pressed]}
  [_ community-id]
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

(rf/defn request-to-join
  {:events [:communities/request-to-join]}
  [_ community-id]
  {:json-rpc/call [{:method      "wakuext_requestToJoinCommunity"
                    :params      [{:communityId community-id}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:communities/requested-to-join %])
                    :on-error    #(log/error "failed to request to join community" community-id)}]})

(rf/defn requested-to-join-with-password-error
  {:events [:communities/requested-to-join-with-password-error]}
  [{:keys [db]} error]
  {:db (assoc-in db [:password-authentication :error] error)})

(rf/defn request-to-join-with-password
  {:events [:communities/request-to-join-with-password]}
  [_ community-id password]
  {:json-rpc/call [{:method      "wakuext_requestToJoinCommunity"
                    :params      [{:communityId community-id :password password}]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:communities/requested-to-join %])
                    :on-error    (fn [error]
                                   (log/error "failed to request to join community" community-id error)
                                   (re-frame/dispatch [:communities/requested-to-join-with-password-error
                                                       error]))}]})

(rf/defn get-user-requests-to-join
  {:events [:communities/get-user-requests-to-join]}
  [_]
  {:json-rpc/call [{:method      "wakuext_myPendingRequestsToJoin"
                    :params      []
                    :js-response true
                    :on-success  #(re-frame/dispatch
                                   [:communities/fetched-my-communities-requests-to-join %])
                    :on-error    #(log/error "failed to get requests to join community")}]})

(rf/defn cancel-request-to-join
  {:events [:communities/cancel-request-to-join]}
  [_ request-to-join-id]
  {:json-rpc/call [{:method      "wakuext_cancelRequestToJoinCommunity"
                    :params      [{:id request-to-join-id}]
                    :on-success  #(re-frame/dispatch [:communities/cancelled-request-to-join %])
                    :js-response true
                    :on-error    #(log/error "failed to cancel request to join community"
                                             request-to-join-id
                                             %)}]})

(rf/defn leave
  {:events [:communities/leave]}
  [{:keys [db]} community-id]
  (let [community-chat-ids (map #(str community-id %)
                                (keys (get-in db [:communities community-id :chats])))]
    {:clear-message-notifications [community-chat-ids
                                   (get-in db [:profile/profile :remote-push-notifications-enabled?])]
     :dispatch                    [:shell/close-switcher-card community-id]
     :json-rpc/call               [{:method      "wakuext_leaveCommunity"
                                    :params      [community-id]
                                    :js-response true
                                    :on-success  #(re-frame/dispatch [::left %])
                                    :on-error    (fn [response]
                                                   (log/error "failed to leave community"
                                                              community-id
                                                              response)
                                                   (re-frame/dispatch [::failed-to-leave]))}]}))

(rf/defn status-tag-pressed
  {:events [:communities/status-tag-pressed]}
  [{:keys [db] :as cofx} community-id literal]
  (let [{:keys [id]} (some #(when (= (:name %) literal) %)
                           (vals (get-in db [:communities community-id :chats])))]
    (when (and id
               (not= (:current-chat-id db) (str community-id id)))
      (chat.events/navigate-to-chat cofx (str community-id id) nil))))

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
            (bottom-sheet/hide-bottom-sheet-old)
            (navigation/open-modal :invite-people-community {:invite? true})))

(rf/defn share-community-pressed
  {:events [:communities/share-community-pressed]}
  [cofx id]
  (rf/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/hide-bottom-sheet-old)
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
  {:events [:legacy-only-for-e2e/open-create-community]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db :communities/create {:membership constants/community-no-membership-access})}
            (navigation/navigate-to :community-create nil)))

(rf/defn create-closed-community
  {:events [:fast-create-community/create-closed-community]}
  [_]
  {:json-rpc/call [{:method      "wakuext_createClosedCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to create closed community." {:error %})}]
   :dispatch      [:hide-bottom-sheet]})

(rf/defn create-open-community
  {:events [:fast-create-community/create-open-community]}
  [_]
  {:json-rpc/call [{:method      "wakuext_createOpenCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to create open community." {:error %})}]
   :dispatch      [:hide-bottom-sheet]})

(rf/defn create-token-gated-community
  {:events [:fast-create-community/create-token-gated-community]}
  [_]
  {:json-rpc/call [{:method      "wakuext_createTokenGatedCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/error "failed to create token gated community." {:error %})}]
   :dispatch      [:hide-bottom-sheet]})

(rf/defn open-edit-community
  {:events [::open-edit-community :communities/open-edit-community]}
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
            (bottom-sheet/hide-bottom-sheet-old)
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
            (bottom-sheet/hide-bottom-sheet-old)
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
        (<-requests-to-join-community-rpc requests :id))})

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
  (let [public-key (get-in db [:profile/profile :public-key])
        hash       (category-hash public-key community-id category-id)]
    {::async-storage/set! {hash state-open?}}))

(rf/defn update-category-states-in-db
  {:events [::category-states-loaded]}
  [{:keys [db]} community-id hashes states]
  (let [public-key     (get-in db [:profile/profile :public-key])
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
  (let [public-key            (get-in db [:profile/profile :public-key])
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

;; Note - dispatch is used to make sure we are opening community once `pop-to-root` is processed.
;; Don't directly merge effects using `navigation/navigate-to`, because it will work in debug and
;; release, but for e2e `pop-to-root` closes even currently opened community
;; https://github.com/status-im/status-mobile/pull/16438#issuecomment-1623954774
(rf/defn navigate-to-community
  {:events [:communities/navigate-to-community]}
  [cofx community-id]
  (rf/merge
   cofx
   {:dispatch [:navigate-to :community-overview community-id]}
   (navigation/pop-to-root :shell-stack)))

(rf/defn member-role-updated
  {:events [:community.member/role-updated]}
  [cofx response-js]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet-old)
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

(rf/defn fetched-collapsed-community-categories
  {:events [:communities/fetched-collapsed-categories-success]}
  [{:keys [db]} categories]
  {:db (assoc db
              :communities/collapsed-categories
              (reduce
               (fn [acc {:keys [communityId categoryId]}]
                 (assoc-in acc [communityId categoryId] true))
               {}
               categories))})

(rf/defn fetch-collapsed-community-categories
  [_]
  {:json-rpc/call [{:method     "wakuext_collapsedCommunityCategories"
                    :params     []
                    :on-success #(re-frame/dispatch
                                  [:communities/fetched-collapsed-categories-success %])
                    :on-error   #(log/error "failed to fetch collapsed community categories"
                                            {:error :%})}]})

(rf/defn toggled-collapsed-category
  {:events [:communities/toggled-collapsed-category-success]}
  [{:keys [db]} community-id category-id collapsed?]
  {:db (assoc-in db [:communities/collapsed-categories community-id category-id] collapsed?)})

(rf/defn toggle-collapsed-category
  {:events [:communities/toggle-collapsed-category]}
  [{:keys [db]} community-id category-id collapse?]
  {:json-rpc/call [{:method     "wakuext_toggleCollapsedCommunityCategory"
                    :params     [{:communityId community-id
                                  :categoryId  category-id
                                  :collapsed   collapse?}]
                    :on-success #(re-frame/dispatch
                                  [:communities/toggled-collapsed-category-success
                                   community-id
                                   category-id
                                   collapse?])
                    :on-error   #(log/error "failed to toggle collapse category"
                                            {:error        %
                                             :community-id community-id
                                             :event        :communities/toggle-collapsed-category
                                             :category-id  category-id
                                             :collapse?    collapse?})}]})

(rf/defn check-and-delete-pending-request-to-join
  {:events [:communities/check-and-delete-pending-request-to-join]}
  [_]
  {:json-rpc/call [{:method      "wakuext_checkAndDeletePendingRequestToJoinCommunity"
                    :params      []
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                    :on-error    #(log/info
                                   "failed to fetch communities"
                                   {:error %
                                    :event
                                    :communities/check-and-delete-pending-request-to-join-community})}]})

(rf/defn mute-community-chats
  {:events [:community/mute-community-chats]}
  [{:keys [db]} chat-id muted? muted-till]
  (log/debug "muted community chat successfully" chat-id muted?)
  {:db (update-in db [:chats chat-id] merge {:muted muted? :muted-till muted-till})})

(rf/defn mute-and-unmute-community-chats
  {:events [:community/update-community-chats-mute-status]}
  [{:keys [db]} community-id muted? mute-till]
  (let [channels (get-in db [:communities community-id :chats])
        chats    (mapv vector (keys channels) (vals channels))]
    (doseq [x chats]
      (doseq [{:keys [id]} x]
        (let [chat-id (str community-id id)]
          (rf/dispatch [:community/mute-community-chats chat-id muted? mute-till]))))))

(rf/defn mute-chat-failed
  {:events [:community/mute-community-failed]}
  [{:keys [db]} community-id muted? error]
  (log/error "mute community failed" community-id error)
  {:db (update-in db [:communities community-id :muted] (not muted?))}
  (rf/dispatch [:community/update-community-chats-mute-status community-id muted? error]))

(rf/defn mute-community-successfully
  {:events [:community/mute-community-successful]}
  [{:keys [db]} community-id muted? muted-till]
  (log/debug "muted community successfully" community-id muted-till)
  (rf/dispatch [:community/update-community-chats-mute-status community-id muted? muted-till])
  (let [time-string (fn [mute-title mute-duration]
                      (i18n/label mute-title {:duration mute-duration}))]
    {:db       (assoc-in db [:communities community-id :muted-till] muted-till)
     :dispatch [:toasts/upsert
                {:icon       :correct
                 :icon-color (quo2.colors/theme-colors
                              quo2.colors/success-60
                              quo2.colors/success-50)
                 :text       (if muted?
                               (when (some? muted-till)
                                 (time-string :t/muted-until (format-mute-till muted-till)))
                               (i18n/label :t/community-unmuted))}]}))


(rf/defn set-community-muted
  {:events [:community/set-muted]}
  [{:keys [db]} community-id muted? muted-type]
  (let [params (if muted? [{:communityId community-id :mutedType muted-type}] [community-id])
        method (if muted? "wakuext_muteCommunityChats" "wakuext_unMuteCommunityChats")]
    {:db            (assoc-in db [:communities community-id :muted] muted?)
     :json-rpc/call [{:method     method
                      :params     params
                      :on-error   #(rf/dispatch [:community/mute-community-failed community-id
                                                 muted? %])
                      :on-success #(rf/dispatch [:community/mute-community-successful
                                                 community-id muted? %])}]}))
