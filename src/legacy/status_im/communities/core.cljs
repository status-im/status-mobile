(ns legacy.status-im.communities.core
  (:require
    [clojure.set :as set]
    legacy.status-im.communities.e2e
    [re-frame.core :as re-frame]
    [status-im.contexts.shell.activity-center.events :as activity-center]
    [status-im.navigation.events :as navigation]
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

(rf/defn handle-response
  [_ response-js]
  {:dispatch [:sanitize-messages-and-process-response response-js]})

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
            (navigation/hide-bottom-sheet)
            (handle-response response-js)
            (activity-center/notifications-fetch-unread-count)))

(rf/defn member-ban
  {:events [::member-ban]}
  [_cofx community-id public-key]
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
            (navigation/hide-bottom-sheet)
            (handle-response response-js)))

(rf/defn member-kick
  {:events [::member-kick]}
  [_cofx community-id public-key]
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
            (navigation/hide-bottom-sheet)
            (handle-response response-js)))

(rf/defn add-role-to-member
  {:events [:community.member/add-role]}
  [_cofx community-id public-key role-id]
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
