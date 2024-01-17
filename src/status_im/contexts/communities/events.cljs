(ns status-im.contexts.communities.events
  (:require [clojure.set :as set]
            [clojure.walk :as walk]
            [legacy.status-im.data-store.chats :as data-store.chats]
            [react-native.platform :as platform]
            [react-native.share :as share]
            [status-im.constants :as constants]
            status-im.contexts.communities.actions.community-options.events
            status-im.contexts.communities.actions.leave.events
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn <-request-to-join-community-rpc
  [r]
  (set/rename-keys r
                   {:communityId :community-id
                    :publicKey   :public-key
                    :chatId      :chat-id}))

(defn <-requests-to-join-community-rpc
  [requests key-fn]
  (reduce #(assoc %1 (key-fn %2) (<-request-to-join-community-rpc %2)) {} requests))

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
  (reduce-kv #(assoc %1 (name %2) %3) {} categ))

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
                        :introMessage                :intro-message
                        :muteTill                    :muted-till})
      (update :admin-settings
              set/rename-keys
              {:pinMessageAllMembersEnabled :pin-message-all-members-enabled?})
      (update :members walk/stringify-keys)
      (update :chats <-chats-rpc)
      (update :token-permissions seq)
      (update :categories <-categories-rpc)
      (assoc :token-images
             (reduce (fn [acc {sym :symbol image :image}]
                       (assoc acc sym image))
                     {}
                     (:communityTokensMetadata c)))))

(rf/reg-event-fx :communities/handle-community
 (fn [{:keys [db]}
      [community-js]]
   (when community-js
     (let [{:keys [token-permissions
                   token-permissions-check joined id]
            :as   community}      (<-rpc community-js)
           has-token-permissions? (not (seq token-permissions))]
       {:db (assoc-in db [:communities id] community)
        :fx [(when (and has-token-permissions? (not joined))
               [:dispatch [:chat.ui/spectate-community id]])
             (when (and has-token-permissions? (nil? token-permissions-check))
               [:dispatch [:communities/check-permissions-to-join-community id]])
             (when (and has-token-permissions? (not (get-in db [:community-channels-permissions id])))
               [:dispatch [:communities/check-all-community-channels-permissions id]])]}))))

(rf/defn handle-removed-chats
  [{:keys [db]} chat-ids]
  {:db (reduce (fn [db chat-id]
                 (update db :chats dissoc chat-id))
               db
               chat-ids)})

(defn- handle-my-request
  [db {:keys [community-id state deleted] :as request}]
  (let [{:keys [name]} (get-in db [:communities community-id])]
    (cond (and (= constants/community-request-to-join-state-pending state) (not deleted))
          (assoc-in db [:communities/my-pending-requests-to-join community-id] request)
          (and (= constants/community-request-to-join-state-accepted state) (not deleted))
          (do (rf/dispatch [:toasts/upsert
                            {:id   :joined-community
                             :type :positive
                             :text (i18n/label :t/joined-community {:community name})}])
              (update-in db [:communities/my-pending-requests-to-join] dissoc community-id))
          :else (update-in db [:communities/my-pending-requests-to-join] dissoc community-id))))

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

(rf/defn handle-communities
  {:events [:community/fetch-success]}
  [{:keys [db]} communities]
  {:fx
   (->> communities
        (map #(vector :dispatch [:communities/handle-community %])))})

(rf/reg-event-fx :communities/request-to-join-result
 (fn [{:keys [db]} [community-id request-id response-js]]
   {:db         (update-in db [:communities/requests-to-join community-id] dissoc request-id)
    :dispatch-n [[:sanitize-messages-and-process-response response-js]
                 [:activity-center.notifications/mark-as-read request-id]]}))

(rf/reg-event-fx :communities/decline-request-to-join-pressed
 (fn [_ [community-id request-id]]
   {:json-rpc/call
    [{:method      "wakuext_declineRequestToJoinCommunity"
      :params      [{:id request-id}]
      :js-response true
      :on-success  #(rf/dispatch [:communities/request-to-join-result community-id request-id %])
      :on-error    #(log/error "failed to decline " community-id request-id)}]}))

(rf/reg-event-fx :communities/accept-request-to-join-pressed
 (fn [_ [community-id request-id]]
   {:json-rpc/call
    [{:method      "wakuext_acceptRequestToJoinCommunity"
      :params      [{:id request-id}]
      :js-response true
      :on-success  #(rf/dispatch [:communities/request-to-join-result community-id request-id %])
      :on-error    #(log/error "failed to accept requests-to-join" community-id request-id %)}]}))

(rf/reg-event-fx :communities/get-user-requests-to-join-success
 (fn [{:keys [db]} [requests]]
   {:db (assoc db
               :communities/my-pending-requests-to-join
               (<-requests-to-join-community-rpc requests :communityId))}))

(rf/reg-event-fx :communities/get-user-requests-to-join
 (fn [_]
   {:json-rpc/call [{:method     "wakuext_myPendingRequestsToJoin"
                     :params     []
                     :on-success #(rf/dispatch [:communities/get-user-requests-to-join-success %])
                     :on-error   #(log/error "failed to get requests to join community")}]}))

(rf/reg-event-fx :communities/fetched-collapsed-categories-success
 (fn [{:keys [db]} [categories]]
   {:db (assoc db
               :communities/collapsed-categories
               (reduce
                (fn [acc {:keys [communityId categoryId]}]
                  (assoc-in acc [communityId categoryId] true))
                {}
                categories))}))

(rf/reg-event-fx :community/fetch
 (fn [_]
   {:json-rpc/call [{:method     "wakuext_communities"
                     :params     []
                     :on-success #(rf/dispatch [:community/fetch-success %])
                     :on-error   #(log/error "failed to fetch communities" %)}
                    {:method      "wakuext_checkAndDeletePendingRequestToJoinCommunity"
                     :params      []
                     :js-response true
                     :on-success  #(rf/dispatch [:sanitize-messages-and-process-response %])
                     :on-error    #(log/info "failed to fetch communities" %)}
                    {:method     "wakuext_collapsedCommunityCategories"
                     :params     []
                     :on-success #(rf/dispatch [:communities/fetched-collapsed-categories-success %])
                     :on-error   #(log/error "failed to fetch collapsed community categories" %)}]}))

(defn initialize-permission-addresses
  [{:keys [db]}]
  (let [accounts        (get-in db [:wallet :accounts])
        sorted-accounts (sort-by :position (vals accounts))
        addresses       (set (map :address sorted-accounts))]
    {:db (assoc db
                :communities/previous-permission-addresses addresses
                :communities/selected-permission-addresses addresses
                :communities/airdrop-address               (:address (first sorted-accounts)))}))

(rf/reg-event-fx :communities/initialize-permission-addresses
 initialize-permission-addresses)

(defn update-previous-permission-addresses
  [{:keys [db]}]
  (let [accounts                      (get-in db [:wallet :accounts])
        sorted-accounts               (sort-by :position (vals accounts))
        selected-permission-addresses (get-in db [:communities/selected-permission-addresses])
        selected-accounts             (filter #(contains? selected-permission-addresses
                                                          (:address %))
                                              sorted-accounts)
        current-airdrop-address       (get-in db [:communities/airdrop-address])]
    {:db (assoc db
                :communities/previous-permission-addresses selected-permission-addresses
                :communities/airdrop-address               (if (contains? selected-permission-addresses
                                                                          current-airdrop-address)
                                                             current-airdrop-address
                                                             (:address (first selected-accounts))))}))

(rf/reg-event-fx :communities/update-previous-permission-addresses
 update-previous-permission-addresses)

(defn toggle-selected-permission-address
  [{:keys [db]} [address]]
  {:db (update db
               :communities/selected-permission-addresses
               (fn [selected-addresses]
                 (if (contains? selected-addresses address)
                   (disj selected-addresses address)
                   (conj selected-addresses address))))})

(rf/reg-event-fx :communities/toggle-selected-permission-address
 toggle-selected-permission-address)

(rf/reg-event-fx :communities/reset-selected-permission-addresses
 (fn [{:keys [db]}]
   {:db (assoc db
               :communities/selected-permission-addresses
               (get-in db [:communities/previous-permission-addresses]))}))

(rf/reg-event-fx :communities/share-community-channel-url-with-data
 (fn [_ [chat-id]]
   (let [{:keys [community-id channel-id]} (data-store.chats/decode-chat-id chat-id)
         title                             (i18n/label :t/channel-on-status)]
     {:json-rpc/call
      [{:method     "wakuext_shareCommunityChannelURLWithData"
        :params     [{:CommunityID community-id :ChannelID channel-id}]
        :on-success (fn [url]
                      (share/open
                       (if platform/ios?
                         {:activityItemSources [{:placeholderItem {:type    "text"
                                                                   :content title}
                                                 :item            {:default {:type    "url"
                                                                             :content url}}
                                                 :linkMetadata    {:title title}}]}
                         {:title     title
                          :subject   title
                          :message   url
                          :url       url
                          :isNewTask true})))
        :on-error   (fn [err]
                      (log/error "failed to retrieve community channel url with data"
                                 {:error   err
                                  :chat-id chat-id
                                  :event   "share-community-channel-url-with-data"}))}]})))

(rf/reg-event-fx :communities/set-airdrop-address
 (fn [{:keys [db]} [address]]
   {:db (assoc db :communities/airdrop-address address)}))
