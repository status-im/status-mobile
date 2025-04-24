(ns status-im.contexts.communities.actions.addresses-for-permissions.events
  (:require
    [schema.core :as schema]
    [status-im.contexts.communities.utils :as utils]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn get-permissioned-balances
  [{[event-id _] :original-event} [community-id]]
  {:fx [[:json-rpc/call
         [{:method     :wakuext_getCommunityPermissionedBalances
           :params     [{:communityId community-id}]
           :on-success [:communities/get-permissioned-balances-success community-id]
           :on-error   (fn [error]
                         (log/error "failed to get balances"
                                    {:community-id community-id
                                     :event        event-id
                                     :error        error}))}]]]})

(schema/=> get-permissioned-balances
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args [:schema [:catn [:community-id :string]]]]]
   [:map {:closed true}
    [:fx
     [:tuple
      [:tuple [:= :json-rpc/call] :schema.common/rpc-call]]]]])

(rf/reg-event-fx :communities/get-permissioned-balances get-permissioned-balances)

(defn get-permissioned-balances-success
  [{:keys [db]} [community-id response]]
  {:db (assoc-in db [:communities/permissioned-balances community-id] response)})

(def ^:private ?account-address keyword?)

(def ^:private ?permissioned-balances-response
  [:map-of
   ?account-address
   [:sequential
    [:map
     [:type int?]
     [:symbol string?]
     [:decimals int?]
     [:amount string?]]]])

(schema/=> get-permissioned-balances-success
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:schema
      [:catn
       [:community-id string?]
       [:response ?permissioned-balances-response]]]]]
   map?])

(rf/reg-event-fx :communities/get-permissioned-balances-success get-permissioned-balances-success)

(rf/reg-event-fx :communities/addresses-for-permissions-cancel-request
 (fn [_ [request-id]]
   {:fx [[:dispatch [:communities/cancel-request-to-join request-id]]
         [:dispatch [:pop-to-root :screen/shell-stack]]
         [:dispatch [:hide-bottom-sheet]]]}))

(rf/reg-event-fx :communities/addresses-for-permissions-leave
 (fn [_ [community-id]]
   {:fx [[:dispatch [:communities/leave community-id]]
         [:dispatch [:hide-bottom-sheet]]
         [:dispatch [:dismiss-modal :screen/addresses-for-permissions]]
         [:dispatch [:pop-to-root :screen/shell-stack]]]}))

(defn check-permissions-to-join-for-selection
  [{:keys [db]} [community-id addresses]]
  (let [request-id (rand-int 10000000)]
    (if (empty? addresses)
      ;; When there are no addresses we can't just check permissions, otherwise
      ;; status-go will consider all possible addresses and the user will see the
      ;; incorrect highest permission role.
      {:db (update db :communities/permissions-checks-for-selection dissoc community-id)}
      {:db (update-in db
                      [:communities/permissions-checks-for-selection community-id]
                      assoc
                      :checking?         true
                      :latest-request-id request-id)
       :fx [[:json-rpc/call
             [{:method     :wakuext_checkPermissionsToJoinCommunity
               :params     [{:communityId community-id :addresses addresses}]
               :id         request-id
               :on-success [:communities/check-permissions-to-join-during-selection-success
                            community-id]
               :on-error   [:communities/check-permissions-to-join-during-selection-failure
                            community-id addresses]}]]]})))

;; This event should be used to check permissions temporarily because it won't
;; mutate the state `:communities/permissions-check` (used by many other
;; screens).
(rf/reg-event-fx :communities/check-permissions-to-join-during-selection
 check-permissions-to-join-for-selection)

(rf/reg-event-fx :communities/check-permissions-to-join-during-selection-success
 (fn [{:keys [db]} [community-id result request-id]]
   (when (= (get-in db [:communities/permissions-checks-for-selection community-id :latest-request-id])
            request-id)
     {:db (assoc-in db
           [:communities/permissions-checks-for-selection community-id]
           {:checking?                  false
            :based-on-client-selection? true
            :check                      result})})))

(rf/reg-event-fx :communities/check-permissions-to-join-during-selection-failure
 (fn [_ [community-id addresses error]]
   (log/error "failed to check permissions for currently selected addresses"
              {:event        :communities/check-permissions-to-join-during-selection
               :community-id community-id
               :addresses    addresses
               :error        error})))

(defn set-permissioned-accounts
  [{:keys [db]} [community-id addresses-to-reveal]]
  (let [addresses-to-reveal     (set addresses-to-reveal)
        wallet-accounts         (utils/sorted-operable-non-watch-only-accounts db)
        current-airdrop-address (get-in db [:communities/all-airdrop-addresses community-id])
        new-airdrop-address     (if (contains? addresses-to-reveal current-airdrop-address)
                                  current-airdrop-address
                                  (->> wallet-accounts
                                       (filter #(contains? addresses-to-reveal (:address %)))
                                       first
                                       :address))]
    {:db (-> db
             (assoc-in [:communities/all-addresses-to-reveal community-id] addresses-to-reveal)
             (assoc-in [:communities/all-airdrop-addresses community-id] new-airdrop-address))
     :fx [[:dispatch
           [:communities/check-permissions-to-join-community
            community-id addresses-to-reveal :based-on-client-selection]]
          [:dispatch [:hide-bottom-sheet]]]}))

(rf/reg-event-fx :communities/set-addresses-to-reveal set-permissioned-accounts)

(defn set-share-all-addresses
  [{:keys [db]} [community-id new-value]]
  (let [current-addresses   (get-in db [:communities/all-addresses-to-reveal community-id])
        addresses-to-reveal (if new-value
                              (->> (utils/sorted-operable-non-watch-only-accounts db)
                                   (map :address)
                                   set)
                              current-addresses)]
    {:db (-> db
             (assoc-in [:communities/selected-share-all-addresses community-id] new-value)
             (assoc-in [:communities/all-addresses-to-reveal community-id] addresses-to-reveal))
     ;; We should check permissions again because the flag is being enabled and
     ;; different addresses will be revealed.
     :fx [(when new-value
            [:dispatch
             [:communities/check-permissions-to-join-during-selection
              community-id addresses-to-reveal]])]}))

(rf/reg-event-fx :communities/set-share-all-addresses set-share-all-addresses)
