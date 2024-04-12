(ns status-im.subs.community.account-selection
  (:require
    [re-frame.core :as re-frame]))

;; This sub is particularly useful in context tags in page-nav or drawer-top
;; components. It should be preferred instead of subscribing to the whole
;; community.
(re-frame/reg-sub :communities/for-context-tag
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/community community-id])])
 (fn [[{:keys [name images color]}]]
   {:name  name
    :logo  (get-in images [:thumbnail :uri])
    :color color}))

(re-frame/reg-sub :communities/addresses-to-reveal
 :<- [:communities/all-addresses-to-reveal]
 (fn [addresses-by-id [_ community-id]]
   (get addresses-by-id community-id)))

(re-frame/reg-sub :communities/airdrop-address
 :<- [:communities/all-airdrop-addresses]
 (fn [addresses-by-id [_ community-id]]
   (get addresses-by-id community-id)))

(re-frame/reg-sub :communities/share-all-addresses?
 :<- [:communities/selected-share-all-addresses]
 (fn [flags-by-id [_ community-id]]
   (get flags-by-id community-id)))

(re-frame/reg-sub :communities/can-edit-shared-addresses?
 (fn [[_ community-id]]
   (re-frame/subscribe [:communities/community community-id]))
 (fn [{:keys [joined]}]
   joined))

(re-frame/reg-sub :communities/permissions-check-for-selection
 :<- [:communities/permissions-checks-for-selection]
 (fn [permissions [_ id]]
   (get permissions id)))

(re-frame/reg-sub :communities/permissions-check-for-selection-checking?
 (fn [[_ community-id]]
   (re-frame/subscribe [:communities/permissions-check-for-selection community-id]))
 (fn [check]
   (:checking? check)))

(re-frame/reg-sub :communities/highest-role-for-selection
 (fn [[_ community-id]]
   [(re-frame/subscribe [:communities/permissions-check-for-selection community-id])])
 (fn [[permissions-check] _]
   (get-in permissions-check [:check :highestRole :type])))

(re-frame/reg-sub :communities/accounts-to-reveal
 (fn [[_ community-id]]
   [(re-frame/subscribe [:wallet/accounts-without-watched-accounts])
    (re-frame/subscribe [:communities/addresses-to-reveal community-id])])
 (fn [[accounts addresses] _]
   (filter #(contains? addresses (:address %))
           accounts)))

(re-frame/reg-sub :communities/airdrop-account
 (fn [[_ community-id]]
   [(re-frame/subscribe [:wallet/accounts-without-watched-accounts])
    (re-frame/subscribe [:communities/airdrop-address community-id])])
 (fn [[accounts airdrop-address] _]
   (->> accounts
        (filter #(= (:address %) airdrop-address))
        first)))

(re-frame/reg-sub :communities/permissioned-balances-by-address
 :<- [:communities/permissioned-balances]
 (fn [balances [_ community-id account-address]]
   (get-in balances [community-id (keyword account-address)])))
