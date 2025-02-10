(ns status-im.contexts.communities.actions.accounts-selection.events
  (:require
    status-im.contexts.communities.actions.accounts-selection.effects
    [status-im.contexts.communities.utils :as utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn initialize-permission-addresses
  [{:keys [db]} [community-id]]
  (let [{:keys [joined]} (get-in db [:communities community-id])]
    {:fx [(if joined
            [:dispatch
             [:communities/get-revealed-accounts community-id
              [:communities/do-init-permission-addresses community-id]]]
            [:dispatch [:communities/do-init-permission-addresses community-id]])]}))

(rf/reg-event-fx :communities/initialize-permission-addresses initialize-permission-addresses)

(defn do-init-permission-addresses
  [{:keys [db]} [community-id revealed-accounts share-future-addresses?]]
  (let [wallet-accounts     (utils/sorted-operable-non-watch-only-accounts db)
        addresses-to-reveal (if (seq revealed-accounts)
                              (set (keys revealed-accounts))
                              ;; Reveal all addresses as fallback.
                              (set (map :address wallet-accounts)))
        ;; When there are no revealed addresses, such as when joining a
        ;; community, use first address for airdrops.
        airdrop-address     (or (->> revealed-accounts
                                     vals
                                     (filter :airdrop-address?)
                                     first
                                     :address)
                                (->> wallet-accounts
                                     first
                                     :address))]
    {:db (-> db
             (assoc-in [:communities/selected-share-all-addresses community-id] share-future-addresses?)
             (assoc-in [:communities/all-addresses-to-reveal community-id] addresses-to-reveal)
             (assoc-in [:communities/all-airdrop-addresses community-id] airdrop-address))
     :fx [[:dispatch
           [:communities/check-permissions-to-join-community
            community-id addresses-to-reveal :based-on-client-selection]]
          ;; Pre-fetch permissions check so that when first opening the
          ;; Addresses for Permissions screen the highest permission role is
          ;; already available and no incorrect information flashes on screen.
          [:dispatch
           [:communities/check-permissions-to-join-during-selection community-id
            addresses-to-reveal]]]}))

(rf/reg-event-fx :communities/do-init-permission-addresses do-init-permission-addresses)

(defn sign-shared-addresses
  "This event will effectively persist the choice of addresses and airdrop address
  in status-go. It can either take addresses to share AND an airdrop address, or
  just addresses to share OR an airdrop address. This is because when the user
  is selecting an airdrop address, we must submit to status-go the current
  choice of addresses to share, and vice-versa. If we omit addresses to share,
  status-go will default to all available."
  [{:keys [db]}
   [{:keys [community-id addresses]} :as args]]
  (let [pub-key             (get-in db [:profile/profile :public-key])
        addresses-to-reveal (if (seq addresses)
                              (set addresses)
                              (get-in db [:communities/all-addresses-to-reveal community-id]))]
    {:fx [[:effects.community/generate-requests-for-signing
           {:pub-key             pub-key
            :community-id        community-id
            :addresses-to-reveal addresses-to-reveal
            :on-success          (fn [signing-requests]
                                   (rf/dispatch
                                    [:standard-auth/authorize-and-sign
                                     {:sign-payload      signing-requests
                                      :on-sign-success   #(rf/dispatch
                                                           [:communities/edit-shared-addresses
                                                            (assoc args :signature-data %)])
                                      :on-sign-error     #(rf/dispatch
                                                           [:communities/edit-shared-addresses-failure
                                                            community-id %])
                                      :auth-button-label (i18n/label :t/confirm-changes)}]))
            :on-error            (fn [error]
                                   (rf/dispatch [:communities/edit-shared-addresses-failure community-id
                                                 error]))}]]}))

(rf/reg-event-fx :communities/sign-shared-addresses sign-shared-addresses)

(defn edit-shared-addresses
  [{:keys [db]}
   [{:keys [community-id signature-data airdrop-address share-future-addresses? on-success]}]]
  (let [pub-key                 (get-in db [:profile/profile :public-key])
        addresses-to-reveal     (set (map :address signature-data))
        new-airdrop-address     (if (contains? addresses-to-reveal airdrop-address)
                                  airdrop-address
                                  (->> (utils/sorted-operable-non-watch-only-accounts db)
                                       (filter #(contains? addresses-to-reveal (:address %)))
                                       first
                                       :address))
        share-future-addresses? (if (nil? share-future-addresses?)
                                  (get-in db [:communities/selected-share-all-addresses community-id])
                                  share-future-addresses?)
        signatures              (utils/extract-join-request-signatures signature-data)]
    {:fx [[:effects.community/edit-shared-addresses
           {:community-id            community-id
            :signatures              signatures
            :pub-key                 pub-key
            :addresses-to-reveal     addresses-to-reveal
            :share-future-addresses? share-future-addresses?
            :airdrop-address         new-airdrop-address
            :on-success              (fn []
                                       (when (fn? on-success)
                                         (on-success addresses-to-reveal
                                                     new-airdrop-address
                                                     share-future-addresses?))
                                       (rf/dispatch [:communities/edit-shared-addresses-success
                                                     community-id addresses-to-reveal
                                                     airdrop-address]))
            :on-error                (fn [error]
                                       [:communities/edit-shared-addresses-failure community-id
                                        error])}]]}))

(rf/reg-event-fx :communities/edit-shared-addresses edit-shared-addresses)

(rf/reg-event-fx :communities/edit-shared-addresses-success
 (fn [_ [community-id addresses-to-reveal airdrop-address share-future-addresses?]]
   {:fx [[:dispatch [:communities/set-airdrop-address community-id airdrop-address]]
         [:dispatch [:communities/set-share-all-addresses community-id share-future-addresses?]]
         [:dispatch [:communities/set-addresses-to-reveal community-id addresses-to-reveal]]]}))

(rf/reg-event-fx :communities/edit-shared-addresses-failure
 (fn [{:keys [db]} [community-id error]]
   (log/error "failed to edit shared addresses"
              {:event        :communities/sign-shared-addresses
               :community-id community-id
               :error        error})
   {:db (assoc-in db [:password-authentication :error] error)}))
