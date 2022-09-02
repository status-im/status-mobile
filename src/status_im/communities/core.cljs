(ns status-im.communities.core
  (:require
   [re-frame.core :as re-frame]
   [clojure.walk :as walk]
   [clojure.string :as string]
   [clojure.set :as clojure.set]
   [taoensso.timbre :as log]
   [status-im.async-storage.core :as async-storage]
   [status-im.utils.fx :as fx]
   [status-im.constants :as constants]
   [status-im.bottom-sheet.core :as bottom-sheet]
   [status-im.utils.universal-links.core :as universal-links]
   [status-im.ethereum.json-rpc :as json-rpc]
   [quo.design-system.colors :as colors]
   [status-im.navigation :as navigation]
   [status-im.utils.handlers :refer [>evt]]
   [status-im.ui.components.emoji-thumbnail.styles :as emoji-thumbnail-styles]
   [status-im.notifications-center.core :as notification-center]))

(def crop-size 1000)

(defn universal-link [community-id]
  (str (:external universal-links/domains)
       "/c/"
       community-id))

(def featured
  [{:name "Status"
    :id constants/status-community-id}])

(defn <-request-to-join-community-rpc [r]
  (clojure.set/rename-keys r {:communityId :community-id
                              :publicKey :public-key
                              :chatId :chat-id}))

(defn <-requests-to-join-community-rpc [requests]
  (reduce (fn [acc r]
            (assoc acc (:id r) (<-request-to-join-community-rpc r)))
          {}
          requests))

(defn <-chats-rpc [chats]
  (reduce-kv (fn [acc k v]
               (assoc acc
                      (name k)
                      (-> v
                          (assoc :can-post? (:canPost v))
                          (dissoc :canPost)
                          (update :members walk/stringify-keys))))
             {}
             chats))

(defn <-categories-rpc [categ]
  (reduce-kv (fn [acc k v]
               (assoc acc
                      (name k)
                      v))
             {}
             categ))

(defn <-rpc [c]
  (-> c
      (clojure.set/rename-keys {:canRequestAccess :can-request-access?
                                :canManageUsers :can-manage-users?
                                :canJoin :can-join?
                                :requestedToJoinAt :requested-to-join-at
                                :isMember :is-member?})
      (update :members walk/stringify-keys)
      (update :chats <-chats-rpc)
      (update :categories <-categories-rpc)))

(defn fetch-community-id-input [{:keys [db]}]
  (:communities/community-id-input db))

(fx/defn handle-request-to-join [{:keys [db]} r]
  (let [{:keys [id community-id] :as request} (<-request-to-join-community-rpc r)]
    {:db (assoc-in db [:communities/requests-to-join community-id id] request)}))

(fx/defn handle-removed-chats [{:keys [db]} chat-ids]
  {:db (reduce (fn [db chat-id]
                 (update db :chats dissoc chat-id))
               db
               chat-ids)})

(fx/defn handle-community
  [{:keys [db]} {:keys [id] :as community}]
  (when id
    {:db (assoc-in db [:communities id] (<-rpc community))}))

(fx/defn handle-communities
  {:events [::fetched]}
  [{:keys [db]} communities]
  {:db (reduce (fn [db {:keys [id] :as community}]
                 (assoc-in db [:communities id] (<-rpc community)))
               db
               communities)})

(fx/defn handle-response [_ response-js]
  {:dispatch [:sanitize-messages-and-process-response response-js]})

(fx/defn left
  {:events [::left]}
  [cofx response-js]
  (fx/merge cofx
            (handle-response cofx response-js)
            (navigation/pop-to-root-tab :chat-stack)
            (notification-center/get-activity-center-notifications-count)))

(fx/defn joined
  {:events [::joined ::requested-to-join]}
  [cofx response-js]
  (handle-response cofx response-js))

(fx/defn export
  {:events [::export-pressed]}
  [cofx community-id]
  {::json-rpc/call [{:method     "wakuext_exportCommunity"
                     :params     [community-id]
                     :on-success #(re-frame/dispatch [:show-popover {:view          :export-community
                                                                     :community-key %}])
                     :on-error   #(do
                                    (log/error "failed to export community" community-id %)
                                    (re-frame/dispatch [::failed-to-export %]))}]})

(fx/defn import-community
  {:events [::import]}
  [cofx community-key]
  {::json-rpc/call [{:method     "wakuext_importCommunity"
                     :params     [community-key]
                     :js-response true
                     :on-success #(re-frame/dispatch [::community-imported %])
                     :on-error   #(do
                                    (log/error "failed to import community" %)
                                    (re-frame/dispatch [::failed-to-import %]))}]})

(fx/defn join
  {:events [::join]}
  [cofx community-id]
  {::json-rpc/call [{:method "wakuext_joinCommunity"
                     :params [community-id]
                     :js-response true
                     :on-success #(re-frame/dispatch [::joined %])
                     :on-error #(do
                                  (log/error "failed to join community" community-id %)
                                  (re-frame/dispatch [::failed-to-join %]))}]})

(fx/defn request-to-join
  {:events [::request-to-join]}
  [cofx community-id]
  {::json-rpc/call [{:method "wakuext_requestToJoinCommunity"
                     :params [{:communityId community-id}]
                     :js-response true
                     :on-success #(re-frame/dispatch [::requested-to-join %])
                     :on-error #(do
                                  (log/error "failed to request to join community" community-id %)
                                  (re-frame/dispatch [::failed-to-request-to-join %]))}]})

(fx/defn leave
  {:events [::leave]}
  [{:keys [db] :as cofx} community-id]
  (let [community-chat-ids (map #(str community-id %)
                                (keys (get-in db [:communities community-id :chats])))]
    {:clear-message-notifications  [community-chat-ids
                                    (get-in db [:multiaccount :remote-push-notifications-enabled?])]
     ::json-rpc/call [{:method     "wakuext_leaveCommunity"
                       :params     [community-id]
                       :js-response true
                       :on-success #(re-frame/dispatch [::left %])
                       :on-error   #(do
                                      (log/error "failed to leave community" community-id %)
                                      (re-frame/dispatch [::failed-to-leave %]))}]}))

(fx/defn fetch [_]
  {::json-rpc/call [{:method "wakuext_communities"
                     :params []
                     :on-success #(re-frame/dispatch [::fetched %])
                     :on-error #(do
                                  (log/error "failed to fetch communities" %)
                                  (re-frame/dispatch [::failed-to-fetch %]))}]})

(fx/defn chat-created
  {:events [::chat-created]}
  [_ community-id user-pk]
  {::json-rpc/call [{:method     "wakuext_sendChatMessage"
                     :params     [{:chatId      user-pk
                                   :text        "Upgrade here to see an invitation to community"
                                   :communityId community-id
                                   :contentType constants/content-type-community}]
                     :js-response true
                     :on-success
                     #(re-frame/dispatch [:transport/message-sent %])
                     :on-error
                     #(log/error "failed to send a message" %)}]})

(fx/defn invite-users
  {:events [::invite-people-confirmation-pressed]}
  [cofx user-pk contacts]
  (let [community-id (fetch-community-id-input cofx)
        pks (if (seq user-pk)
              (conj contacts user-pk)
              contacts)]
    (when (seq pks)
      {::json-rpc/call [{:method     "wakuext_inviteUsersToCommunity"
                         :params     [{:communityId community-id
                                       :users pks}]
                         :js-response true
                         :on-success #(re-frame/dispatch [::people-invited %])
                         :on-error   #(do
                                        (log/error "failed to invite-user community" %)
                                        (re-frame/dispatch [::failed-to-invite-people %]))}]})))
(fx/defn share-community
  {:events [::share-community-confirmation-pressed]}
  [cofx user-pk contacts]
  (let [community-id (fetch-community-id-input cofx)
        pks (if (seq user-pk)
              (conj contacts user-pk)
              contacts)]
    (when (seq pks)
      {::json-rpc/call [{:method     "wakuext_shareCommunity"
                         :params     [{:communityId community-id
                                       :users pks}]
                         :js-response true
                         :on-success #(re-frame/dispatch [::people-invited %])
                         :on-error   #(do
                                        (log/error "failed to invite-user community" %)
                                        (re-frame/dispatch [::failed-to-share-community %]))}]})))

(fx/defn create
  {:events [::create-confirmation-pressed]}
  [{:keys [db]}]
  (let [{:keys [name description membership image]} (get db :communities/create)]
    (let [params {:name name
                  :description description
                  :membership membership
                  :color (rand-nth colors/chat-colors)
                  :image (string/replace-first (str image) #"file://" "")
                  :imageAx 0
                  :imageAy 0
                  :imageBx crop-size
                  :imageBy crop-size}]

      {::json-rpc/call [{:method     "wakuext_createCommunity"
                         :params     [params]
                         :js-response true
                         :on-success #(re-frame/dispatch [::community-created %])
                         :on-error   #(do
                                        (log/error "failed to create community" %)
                                        (re-frame/dispatch [::failed-to-create-community %]))}]})))

(fx/defn edit
  {:events [::edit-confirmation-pressed]}
  [{:keys [db]}]
  (let [{:keys [id name description membership new-image color]} (get db :communities/create)]
    {::json-rpc/call [{:method     "wakuext_editCommunity"
                       :params     [{:communityID id
                                     :name name
                                     :description description
                                     :color color
                                     :image (string/replace-first (str new-image) #"file://" "")
                                     :imageAx 0
                                     :imageAy 0
                                     :imageBx crop-size
                                     :imageBy crop-size
                                     :membership membership}]
                       :on-success #(re-frame/dispatch [::community-edited %])
                       :on-error   #(do
                                      (log/error "failed to edit community" %)
                                      (re-frame/dispatch [::failed-to-edit-community %]))}]}))

(fx/defn create-channel
  {:events [::create-channel-confirmation-pressed]}
  [{:keys [db] :as cofx}]
  (let [community-id (fetch-community-id-input cofx)
        {:keys [name description color emoji]} (get db :communities/create-channel)]
    {::json-rpc/call [{:method     "wakuext_createCommunityChat"
                       :params     [community-id
                                    {:identity    {:display_name name
                                                   :description  description
                                                   :color        color
                                                   :emoji        emoji}
                                     :permissions {:access constants/community-channel-access-no-membership}}]
                       :js-response true
                       :on-success #(re-frame/dispatch [::community-channel-created %])
                       :on-error   #(do
                                      (log/error "failed to create community channel" %)
                                      (re-frame/dispatch [::failed-to-create-community-channel %]))}]}))

(def community-chat-id-length 68)

(defn to-community-chat-id [chat-id]
  (subs chat-id community-chat-id-length))

(fx/defn edit-channel
  {:events [::edit-channel-confirmation-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [name description color community-id emoji edit-channel-id category-id position]}
        (get db :communities/create-channel)]
    {::json-rpc/call [{:method     "wakuext_editCommunityChat"
                       :params     [community-id
                                    edit-channel-id
                                    {:identity    {:display_name name
                                                   :description  description
                                                   :color        color
                                                   :emoji        emoji}
                                     :category_id  category-id
                                     :position     position
                                     :permissions {:access constants/community-channel-access-no-membership}}]
                       :js-response true
                       :on-success #(re-frame/dispatch [::community-channel-edited %])
                       :on-error   #(do
                                      (log/error "failed to edit community channel" %)
                                      (re-frame/dispatch [::failed-to-edit-community-channel %]))}]}))

(defn require-membership? [permissions]
  (not= constants/community-no-membership-access (:access permissions)))

(defn can-post? [community _ local-chat-id]
  (let [chat-id (to-community-chat-id local-chat-id)]
    (get-in community [:chats chat-id :can-post?])))

(fx/defn reset-community-id-input [{:keys [db]} id]
  {:db (assoc db :communities/community-id-input id)})

(fx/defn reset-channel-info [{:keys [db]}]
  {:db (assoc db :communities/create-channel {})})

(fx/defn invite-people-pressed
  {:events [::invite-people-pressed]}
  [cofx id]
  (fx/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/hide-bottom-sheet)
            (navigation/open-modal :invite-people-community {:invite? true})))

(fx/defn share-community-pressed
  {:events [::share-community-pressed]}
  [cofx id]
  (fx/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/hide-bottom-sheet)
            (navigation/open-modal :invite-people-community {})))

(fx/defn create-channel-pressed
  {:events [::create-channel-pressed]}
  [{:keys [db] :as cofx} id]
  (fx/merge cofx
            (reset-community-id-input id)
            (reset-channel-info)
            (>evt [::create-channel-fields (rand-nth emoji-thumbnail-styles/emoji-picker-default-thumbnails)])
            (navigation/open-modal :create-community-channel {:community-id id})))

(fx/defn edit-channel-pressed
  {:events [::edit-channel-pressed]}
  [{:keys [db] :as cofx} community-id chat-name description color emoji chat-id category-id position]
  (let [{:keys [color emoji]} (if (string/blank? emoji)
                                (rand-nth emoji-thumbnail-styles/emoji-picker-default-thumbnails)
                                {:color color :emoji emoji})]
    (fx/merge cofx
              {:db (assoc db :communities/create-channel {:name            chat-name
                                                          :description     description
                                                          :color           color
                                                          :community-id    community-id
                                                          :emoji           emoji
                                                          :edit-channel-id chat-id
                                                          :category-id     category-id
                                                          :position        position})}
              (navigation/open-modal :edit-community-channel nil))))

(fx/defn community-created
  {:events [::community-created]}
  [cofx response-js]
  (fx/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(fx/defn community-edited
  {:events [::community-edited]}
  [cofx response-js]
  (fx/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(fx/defn open-create-community
  {:events [::open-create-community]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db :communities/create {:membership constants/community-no-membership-access})}
            (navigation/navigate-to :community-create nil)))

(fx/defn open-edit-community
  {:events [::open-edit-community]}
  [{:keys [db] :as cofx} id]
  (let [{:keys [name description images permissions color]} (get-in db [:communities id])
        {:keys [access]}                                   permissions]
    (fx/merge cofx
              {:db (assoc db :communities/create {:id          id
                                                  :name        name
                                                  :description description
                                                  :image       (get-in images [:large :uri])
                                                  :membership  access
                                                  :color       color
                                                  :editing?    true})}
              (navigation/navigate-to :community-edit nil))))

(fx/defn community-imported
  {:events [::community-imported]}
  [cofx response-js]
  (fx/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(fx/defn people-invited
  {:events [::people-invited]}
  [cofx response-js]
  (fx/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(fx/defn community-channel-created
  {:events [::community-channel-created]}
  [cofx response-js]
  (fx/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(fx/defn community-channel-edited
  {:events [::community-channel-edited]}
  [cofx response-js]
  (fx/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

(fx/defn create-field
  {:events [::create-field]}
  [{:keys [db]} field value]
  {:db (assoc-in db [:communities/create field] value)})

(fx/defn remove-field
  {:events [::remove-field]}
  [{:keys [db]} field]
  {:db (update-in db [:communities/create] dissoc field)})

(fx/defn create-channel-field
  {:events [::create-channel-field]}
  [{:keys [db]} field value]
  {:db (assoc-in db [:communities/create-channel field] value)})

(fx/defn create-channel-fields
  {:events [::create-channel-fields]}
  [{:keys [db]} field-values]
  {:db (update-in db [:communities/create-channel] merge field-values)})

(fx/defn member-banned
  {:events [::member-banned]}
  [cofx response-js]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response-js)
            (notification-center/get-activity-center-notifications-count)))

(fx/defn member-ban
  {:events [::member-ban]}
  [cofx community-id public-key]
  {::json-rpc/call [{:method     "wakuext_banUserFromCommunity"
                     :params     [{:communityId community-id
                                   :user public-key}]
                     :js-response true
                     :on-success #(re-frame/dispatch [::member-banned %])
                     :on-error   #(log/error "failed to ban user from community" community-id public-key %)}]})

(fx/defn member-kicked
  {:events [::member-kicked]}
  [cofx response-js]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response-js)))

(fx/defn member-kick
  {:events [::member-kick]}
  [cofx community-id public-key]
  {::json-rpc/call [{:method     "wakuext_removeUserFromCommunity"
                     :params     [community-id public-key]
                     :js-response true
                     :on-success #(re-frame/dispatch [::member-kicked %])
                     :on-error   #(log/error "failed to remove user from community" community-id public-key %)}]})

(fx/defn delete-community
  {:events [::delete-community]}
  [cofx community-id]
  (log/error "Community delete is not yet implemented"))

(fx/defn requests-to-join-fetched
  {:events [::requests-to-join-fetched]}
  [{:keys [db]} community-id requests]
  {:db (assoc-in db [:communities/requests-to-join community-id] (<-requests-to-join-community-rpc requests))})

(fx/defn fetch-requests-to-join
  {:events [::fetch-requests-to-join]}
  [cofx community-id]
  {::json-rpc/call [{:method     "wakuext_pendingRequestsToJoinForCommunity"
                     :params     [community-id]
                     :on-success #(re-frame/dispatch [::requests-to-join-fetched community-id %])
                     :on-error   #(log/error "failed to fetch requests-to-join" community-id %)}]})

(defn fetch-requests-to-join! [community-id]
  (re-frame/dispatch [::fetch-requests-to-join community-id]))

(fx/defn request-to-join-accepted
  {:events [::request-to-join-accepted]}
  [{:keys [db] :as cofx} community-id request-id response-js]
  (fx/merge cofx
            {:db (update-in db [:communities/requests-to-join community-id] dissoc request-id)}
            (handle-response response-js)))

(fx/defn request-to-join-declined
  {:events [::request-to-join-declined]}
  [{:keys [db] :as cofx} community-id request-id response-js]
  (fx/merge cofx
            {:db (update-in db [:communities/requests-to-join community-id] dissoc request-id)}
            (handle-response response-js)))

(fx/defn accept-request-to-join-pressed
  {:events [:communities.ui/accept-request-to-join-pressed]}
  [cofx community-id request-id]
  {::json-rpc/call [{:method     "wakuext_acceptRequestToJoinCommunity"
                     :params     [{:id request-id}]
                     :js-response true
                     :on-success #(re-frame/dispatch [::request-to-join-accepted community-id request-id %])
                     :on-error   #(log/error "failed to accept requests-to-join" community-id request-id %)}]})

(fx/defn decline-request-to-join-pressed
  {:events [:communities.ui/decline-request-to-join-pressed]}
  [cofx community-id request-id]
  {::json-rpc/call [{:method     "wakuext_declineRequestToJoinCommunity"
                     :params     [{:id request-id}]
                     :js-response true
                     :on-success #(re-frame/dispatch [::request-to-join-declined community-id request-id %])
                     :on-error   #(log/error "failed to decline requests-to-join" community-id request-id)}]})

(fx/defn switch-communities-enabled
  {:events [:multiaccounts.ui/switch-communities-enabled]}
  [{:keys [db]} enabled?]
  {::async-storage/set! {:communities-enabled? enabled?}
   :db (assoc db :communities/enabled? enabled?)})

(fx/defn create-category
  {:events [::create-category-confirmation-pressed]}
  [_ community-id category-title chat-ids]
  {::json-rpc/call [{:method     "wakuext_createCommunityCategory"
                     :params     [{:communityId community-id
                                   :categoryName category-title
                                   :chatIds (map #(string/replace % community-id "") chat-ids)}]
                     :js-response true
                     :on-success #(do
                                    (re-frame/dispatch [:navigate-back])
                                    (re-frame/dispatch [:sanitize-messages-and-process-response %]))
                     :on-error   #(log/error "failed to create community category" %)}]})

(fx/defn remove-chat-from-category
  {:events [:remove-chat-from-community-category]}
  [{:keys [db]} community-id id categoryID]
  (let [category (get-in db [:communities community-id :categories categoryID])
        category-chats (map :id (filter #(and (= (:categoryID %) categoryID) (not= id (:id %)))
                                        (vals (get-in db [:communities community-id :chats]))))]
    {::json-rpc/call [{:method     "wakuext_editCommunityCategory"
                       :params     [{:communityId community-id
                                     :categoryId categoryID
                                     :categoryName (:name category)
                                     :chatIds category-chats}]
                       :js-response true
                       :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                       :on-error   #(log/error "failed to remove chat from community" %)}]}))

(fx/defn delete-community-chat
  {:events [:delete-community-chat]}
  [_ community-id chat-id]
  {::json-rpc/call [{:method     "wakuext_deleteCommunityChat"
                     :params     [community-id chat-id]
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error   #(log/error "failed to delete community chat" %)}]})

(fx/defn delete-category
  {:events [:delete-community-category]}
  [_ community-id category-id]
  {::json-rpc/call [{:method     "wakuext_deleteCommunityCategory"
                     :params     [{:communityId community-id
                                   :categoryId category-id}]
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error   #(log/error "failed to delete community category" %)}]})

(fx/defn change-category
  {:events [::change-category-confirmation-pressed]}
  [cofx community-id category-id {:keys [id position categoryID]}]
  (if (not (string/blank? category-id))
    {::json-rpc/call [{:method     "wakuext_reorderCommunityChat"
                       :params     [{:communityId community-id
                                     :categoryId category-id
                                     :chatId id
                                     :position position}]
                       :js-response true
                       :on-success #(do
                                      (re-frame/dispatch [:navigate-back])
                                      (re-frame/dispatch [:sanitize-messages-and-process-response %]))
                       :on-error   #(log/error "failed to change community category" %)}]}
    (fx/merge cofx
              (navigation/navigate-back)
              (remove-chat-from-category community-id id categoryID))))

(fx/defn reorder-category-chat
  {:events [::reorder-community-category-chat]}
  [_ community-id category-id chat-id new-position]
  {::json-rpc/call [{:method     "wakuext_reorderCommunityChat"
                     :params     [{:communityId community-id
                                   :categoryId  category-id
                                   :chatId      chat-id
                                   :position    new-position}]
                     :js-response true
                     :on-success #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error   #(log/error "failed to reorder community category chat" %)}]})

(fx/defn reorder-category
  {:events [::reorder-community-category]}
  [_ community-id category-id new-position]
  {::json-rpc/call [{:method     "wakuext_reorderCommunityCategories"
                     :params     [{:communityId community-id
                                   :categoryId  category-id
                                   :position    new-position}]
                     :js-response true
                     :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])
                     :on-error    #(log/error "failed to reorder community category" %)}]})

(defn category-hash [public-key community-id category-id]
  (hash (str public-key community-id category-id)))

(fx/defn store-category-state
  {:events [::store-category-state]}
  [{:keys [db]} community-id category-id state-open?]
  (let [public-key (get-in db [:multiaccount :public-key])
        hash       (category-hash public-key community-id category-id)]
    {::async-storage/set! {hash state-open?}}))

(fx/defn update-category-states-in-db
  {:events [::category-states-loaded]}
  [{:keys [db]} community-id hashes states]
  (let [public-key     (get-in db [:multiaccount :public-key])
        categories-old (get-in db [:communities community-id :categories])
        categories     (reduce (fn [acc [category-id category]]
                                 (let [hash             (get hashes category-id)
                                       state            (get states hash)
                                       category-updated (assoc category :state state)]
                                   (assoc acc category-id category-updated)))
                               {} categories-old)]
    {:db (update-in db [:communities community-id :categories] merge categories)}))

(fx/defn load-category-states
  {:events [::load-category-states]}
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
                                      {} categories)]
    {::async-storage/get {:keys keys
                          :cb   #(re-frame/dispatch
                                  [::category-states-loaded community-id hashes %])}}))
