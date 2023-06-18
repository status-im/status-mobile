(ns status-im2.contexts.communities.overview.events
  (:require [utils.re-frame :as rf]
            [taoensso.timbre :as log]))

(rf/defn check-permissions-to-join-community-success
  {:events [:communities/check-permissions-to-join-community-success]}
  [{:keys [db]} community-id result]
  {:db (-> db
           (assoc-in [:communities community-id :checking-permissions?] false)
           (assoc-in [:communities community-id :token-permissions-check] result))})

(rf/defn check-permissions-to-join-community
  {:events [:communities/check-permissions-to-join-community]}
  [{:keys [db]} community-id]
  {:db            (-> db
                      (assoc-in [:communities community-id :checking-permissions?] true)
                      (assoc-in [:communities community-id :can-request-access?] false))
   :json-rpc/call [{:method     "wakuext_checkPermissionsToJoinCommunity"
                    :params     [{:communityId community-id}]
                    :on-success #(rf/dispatch [:communities/check-permissions-to-join-community-success
                                               community-id %])
                    :on-error   #(log/error "failed to request to join community" community-id %)}]})
