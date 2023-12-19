(ns status-im2.contexts.communities.overview.events
  (:require
    [legacy.status-im.data-store.communities :as data-store]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :communities/check-all-community-channels-permissions-success
 (fn [{:keys [db]} [community-id response]]
   {:db (assoc-in db
         [:community-channels-permissions community-id]
         (data-store/rpc->channel-permissions (:channels response)))}))

(rf/reg-event-fx :communities/check-all-community-channels-permissions
 (fn [_ [community-id]]
   {:fx [[:json-rpc/call
          [{:method     "wakuext_checkAllCommunityChannelsPermissions"
            :params     [{:CommunityID community-id}]
            :on-success [:communities/check-all-community-channels-permissions-success community-id]
            :on-error   (fn [error]
                          (log/error "failed to check channels permissions"
                                     {:error error
                                      :community-id community-id
                                      :event
                                      :communities/check-all-community-channels-permissions}))}]]]}))

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

(defn request-to-join
  [{:keys [db]} [{:keys [community-id password]}]]
  (let [pub-key             (get-in db [:profile/profile :public-key])
        addresses-to-reveal []]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_generateJoiningCommunityRequestsForSigning"
             :params     [pub-key community-id addresses-to-reveal]
             :on-success [:communities/sign-data community-id password]
             :on-error   [:communities/requested-to-join-error community-id]}]]]}))

;; Event to be called to request to join a community.
;; This event will generate the data to be signed and then call the sign-data event.
;; This is the only event that should be called from the UI.
(rf/reg-event-fx :communities/request-to-join request-to-join)

(defn sign-data
  [_ [community-id password sign-params]]
  (let [addresses-to-reveal (map :account sign-params)]
    {:fx [[:json-rpc/call
           [{:method     "wakuext_signData"
             :params     [(map #(assoc % :password password) sign-params)]
             :on-success [:communities/request-to-join-with-signatures community-id addresses-to-reveal]
             :on-error   [:communities/requested-to-join-error community-id]}]]]}))

(rf/reg-event-fx :communities/sign-data sign-data)

(rf/reg-event-fx :communities/requested-to-join-error
 (fn [{:keys [db]} [community-id error]]
   (log/error "failed to request to join community"
              {:community-id community-id
               :error        error
               :event        :communities/requested-to-join-error})
   {:db (assoc-in db [:password-authentication :error] error)}))

(defn request-to-join-with-signatures
  [_ [community-id addresses-to-reveal signatures]]
  {:fx [[:json-rpc/call
         [{:method      "wakuext_requestToJoinCommunity"
           :params      [{:communityId       community-id
                          :signatures        signatures
                          :addressesToReveal addresses-to-reveal
                          ;; NOTE: At least one airdrop address is required.
                          ;; This is a temporary solution while the address
                          ;; selection feature is not implemented in mobile.
                          :airdropAddress    (first addresses-to-reveal)}]
           :js-response true
           :on-success  [:communities/requested-to-join]
           :on-error    [:communities/requested-to-join-error community-id]}]]]})

(rf/reg-event-fx :communities/request-to-join-with-signatures request-to-join-with-signatures)
