(ns status-im2.contexts.communities.overview.events
  (:require
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

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

;; Event to be called to request to join a community.
;; This event will generate the data to be signed and then call the sign-data event.
;; This is the only event that should be called from the UI.
(rf/reg-event-fx :communities/request-to-join
 (fn [{:keys [db]} [{:keys [community-id password]}]]
   (let [pub-key             (get-in db [:profile/profile :public-key])
         addresses-to-reveal []]
     {:fx [[:json-rpc/call
            [{:method     "wakuext_generateJoiningCommunityRequestsForSigning"
              :params     [pub-key community-id addresses-to-reveal]
              :on-success [:communities/sign-data community-id password]
              :on-error   [:communities/requested-to-join-error community-id]}]]]})))

(rf/reg-event-fx :communities/sign-data
 (fn [_ [community-id password sign-params]]
   {:fx [[:json-rpc/call
          [{:method     "wakuext_signData"
            :params     [(map #(assoc % :password password) sign-params)]
            :on-success [:communities/request-to-join-with-signatures community-id]
            :on-error   [:communities/requested-to-join-error community-id]}]]]}))

(rf/reg-event-fx :communities/requested-to-join-error
 (fn [{:keys [db]} [community-id error]]
   (log/error "failed to request to join community"
              {:community-id community-id
               :error        error
               :event        :communities/requested-to-join-error})
   {:db (assoc-in db [:password-authentication :error] error)}))

(rf/reg-event-fx :communities/request-to-join-with-signatures
 (fn [_ [community-id signatures]]
   {:fx [[:json-rpc/call
          [{:method      "wakuext_requestToJoinCommunity"
            :params      [{:communityId community-id :signatures signatures}]
            :js-response true
            :on-success  [:communities/requested-to-join]
            :on-error    [:communities/requested-to-join-error community-id]}]]]}))
