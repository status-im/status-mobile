(ns legacy.status-im.communities.core
  (:require
    [clojure.set :as set]
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    legacy.status-im.communities.e2e
    [re-frame.core :as re-frame]
    [status-im2.contexts.shell.activity-center.events :as activity-center]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

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

(defn- fetch-community-id-input
  [{:keys [db]}]
  (:communities/community-id-input db))

(rf/defn handle-response
  [_ response-js]
  {:dispatch [:sanitize-messages-and-process-response response-js]})

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

(re-frame/reg-event-fx :communities/invite-people-pressed
 (fn [{:keys [db]} [id]]
   {:db (assoc db :communities/community-id-input id)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:open-modal :legacy-invite-people-community {:invite? true}]]]}))

(re-frame/reg-event-fx :communities/share-community-pressed
 (fn [{:keys [db]} [id]]
   {:db (assoc db :communities/community-id-input id)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:open-modal :legacy-invite-people-community {}]]]}))

(rf/defn people-invited
  {:events [::people-invited]}
  [cofx response-js]
  (rf/merge cofx
            (navigation/navigate-back)
            (handle-response response-js)))

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

(rf/defn requests-to-join-fetched
  {:events [::requests-to-join-fetched]}
  [{:keys [db]} community-id requests]
  {:db (assoc-in db
        [:communities/requests-to-join community-id]
        (<-requests-to-join-community-rpc requests :id))})

(rf/defn fetch-requests-to-join
  {:events [:community/fetch-requests-to-join]}
  [_ community-id]
  {:json-rpc/call [{:method     "wakuext_pendingRequestsToJoinForCommunity"
                    :params     [community-id]
                    :on-success #(re-frame/dispatch [::requests-to-join-fetched community-id %])
                    :on-error   #(log/error "failed to fetch requests-to-join" community-id %)}]})

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
