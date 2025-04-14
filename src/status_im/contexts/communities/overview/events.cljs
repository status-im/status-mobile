(ns status-im.contexts.communities.overview.events
  (:require
    [status-im.contexts.communities.utils :as utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :communities/check-permissions-to-join-community-success
 (fn [{:keys [db]} [community-id based-on-client-selection? result]]
   (let [token-permissions-check (cond-> result
                                   based-on-client-selection? (assoc :based-on-client-selection? true))]
     {:db (-> db
              (assoc-in [:communities/permissions-check community-id]
                        {:checking? false
                         :check     token-permissions-check}))})))

(rf/reg-event-fx :communities/check-permissions-to-join-community-failed
 (fn [{:keys [db]} [community-id]]
   {:db (assoc-in db [:communities/permissions-check community-id :checking?] false)}))

(rf/reg-event-fx :communities/check-permissions-to-join-community
 (fn [{:keys [db]} [community-id addresses based-on-client-selection?]]
   (when-let [community (get-in db [:communities community-id])]
     (when-not (:checking-permissions? community)
       {:db            (assoc-in db [:communities/permissions-check community-id :checking?] true)
        :json-rpc/call [{:method     "wakuext_checkPermissionsToJoinCommunity"
                         :params     [(cond-> {:communityId community-id}
                                        addresses
                                        (assoc :addresses addresses))]
                         :on-success [:communities/check-permissions-to-join-community-success
                                      community-id based-on-client-selection?]
                         :on-error   (fn [err]
                                       (rf/dispatch
                                        [:communities/check-permissions-to-join-community-failed
                                         community-id])
                                       (log/error "failed to request to join community"
                                                  community-id
                                                  err))}]}))))

(rf/reg-event-fx :communities/check-permissions-to-join-community-with-all-addresses-success
 (fn [{:keys [db]} [community-id result]]
   {:db (assoc-in db
         [:communities/permissions-check-all community-id]
         {:checking? false
          :check     result})}))

(rf/reg-event-fx :communities/check-permissions-to-join-community-with-all-addresses-failed
 (fn [{:keys [db]} [community-id]]
   {:db (assoc-in db [:communities/permissions-check-all community-id :checking?] false)}))

(rf/reg-event-fx :communities/check-permissions-to-join-community-with-all-addresses
 (fn [{:keys [db]} [community-id]]
   (let [accounts  (utils/sorted-operable-non-watch-only-accounts db)
         addresses (set (map :address accounts))]
     {:db            (assoc-in db [:communities/permissions-check community-id :checking?] true)
      :json-rpc/call [{:method "wakuext_checkPermissionsToJoinCommunity"
                       :params [(cond-> {:communityId community-id}
                                  (seq addresses)
                                  (assoc :addresses addresses))]
                       :on-success
                       [:communities/check-permissions-to-join-community-with-all-addresses-success
                        community-id]
                       :on-error
                       (fn [err]
                         (rf/dispatch
                          [:communities/check-permissions-to-join-community-with-all-addresses-failed
                           community-id])
                         (log/error "failed to check permissions for all addresses"
                                    community-id
                                    err))}]})))

(rf/reg-event-fx :communities/requested-to-join-error
 (fn [{:keys [db]} [community-id error]]
   (log/error "failed to request to join community"
              {:community-id community-id
               :error        error
               :event        :communities/requested-to-join-error})
   {:db (assoc-in db [:password-authentication :error] error)}))

(rf/reg-event-fx :communities/requested-to-join
 (fn [_ [response-js]]
   (let [community-name (aget response-js "communities" 0 "name")]
     {:fx [[:dispatch [:sanitize-messages-and-process-response response-js]]
           [:dispatch [:hide-bottom-sheet]]
           [:dispatch
            [:toasts/upsert
             {:type :positive
              :text (i18n/label
                     :t/requested-to-join-community
                     {:community community-name})}]]]})))

(rf/reg-event-fx :communities/toggled-collapsed-category-success
 (fn [{:keys [db]} [community-id category-id collapsed?]]
   {:db (assoc-in db [:communities/collapsed-categories community-id category-id] collapsed?)}))

(rf/reg-event-fx :communities/toggle-collapsed-category
 (fn [_ [community-id category-id collapse?]]
   {:json-rpc/call
    [{:method     "wakuext_toggleCollapsedCommunityCategory"
      :params     [{:communityId community-id
                    :categoryId  category-id
                    :collapsed   collapse?}]
      :on-success #(rf/dispatch
                    [:communities/toggled-collapsed-category-success community-id category-id collapse?])
      :on-error   #(log/error "failed to toggle collapse category"
                              {:error        %
                               :community-id community-id
                               :event        :communities/toggle-collapsed-category
                               :category-id  category-id
                               :collapse?    collapse?})}]}))

(defn generate-requests-for-signing
  [{:keys [db]} [community-id]]
  (let [pub-key             (get-in db [:profile/profile :public-key])
        addresses-to-reveal (get-in db [:communities/all-addresses-to-reveal community-id])]
    {:fx [[:effects.community/generate-requests-for-signing
           {:pub-key             pub-key
            :community-id        community-id
            :addresses-to-reveal addresses-to-reveal
            :on-success          #(rf/dispatch [:communities/on-join-requests-prepared
                                                community-id
                                                %])}]]}))

(rf/reg-event-fx :communities/generate-requests-for-signing generate-requests-for-signing)

(defn on-join-requests-prepared
  [{:keys [db]} [community-id signing-requests]]
  {:db (assoc-in db
        [:communities/join-requests-for-signing community-id]
        signing-requests)})

(rf/reg-event-fx :communities/on-join-requests-prepared on-join-requests-prepared)

(defn request-to-join
  [{:keys [db]} [{:keys [community-id signature-data]}]]
  (let [share-future-addresses? (get-in db [:communities/selected-share-all-addresses community-id])
        airdrop-address         (get-in db [:communities/all-airdrop-addresses community-id])
        addresses-to-reveal     (map :address signature-data)
        signatures              (utils/extract-join-request-signatures signature-data)]
    {:fx [[:effects.community/request-to-join
           {:community-id            community-id
            :signatures              signatures
            :addresses-to-reveal     addresses-to-reveal
            :airdrop-address         airdrop-address
            :share-future-addresses? share-future-addresses?
            :on-success              #(rf/dispatch [:communities/requested-to-join %])
            :on-error                #(rf/dispatch [:communities/requested-to-join-error community-id
                                                    %])}]]}))

(rf/reg-event-fx :communities/request-to-join request-to-join)
