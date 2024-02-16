(ns status-im.contexts.communities.actions.addresses-for-permissions.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [status-im.contexts.communities.actions.addresses-for-permissions.style :as style]
            [status-im.contexts.communities.utils :as communities.utils]
            [utils.i18n :as i18n]
            [utils.money :as money]
            [utils.re-frame :as rf]))

(defn- balances->components-props
  [balances]
  (for [{:keys [amount decimals type name] :as balance} balances]
    (cond-> balance
      true
      (assoc :type
             (condp = type
               constants/community-token-type-erc20  :token
               constants/community-token-type-erc721 :collectible
               :token))

      (= type constants/community-token-type-erc721)
      (assoc :collectible-name    name
             :collectible-img-src (resources/get-mock-image :collectible))

      (= type constants/community-token-type-erc20)
      (assoc :amount (str (money/token->unit amount decimals))
             :token  (:symbol balance)))))

(defn- account-item
  [{:keys [color address name emoji]} _ _
   {:keys [selected-addresses community-id share-all-addresses? community-color]}]
  (let [balances (rf/sub [:communities/permissioned-balances-by-address community-id address])]
    [quo/account-permissions
     {:account             {:name                name
                            :address             address
                            :emoji               emoji
                            :customization-color color}
      :token-details       (balances->components-props balances)
      :checked?            (contains? selected-addresses address)
      :disabled?           share-all-addresses?
      :on-change           #(rf/dispatch [:communities/toggle-selected-permission-address
                                          address community-id])
      :container-style     {:margin-bottom 8}
      :customization-color community-color}]))

(defn view
  [{:keys [scroll-enabled? on-scroll]}]
  (let [{id :community-id} (rf/sub [:get-screen-params])]
    (rf/dispatch [:communities/get-permissioned-balances id])
    (fn []
      (let [{:keys [name color images]}       (rf/sub [:communities/community id])
            {:keys [highest-permission-role]} (rf/sub [:community/token-gated-overview id])
            accounts                          (rf/sub [:wallet/accounts-without-watched-accounts])
            selected-addresses                (rf/sub [:communities/selected-permission-addresses id])
            share-all-addresses?              (rf/sub [:communities/share-all-addresses? id])
            unsaved-address-changes?          (rf/sub [:communities/unsaved-address-changes? id])
            highest-role-text                 (when highest-permission-role
                                                (i18n/label (communities.utils/role->translation-key
                                                             highest-permission-role)))]
        [rn/safe-area-view {:style style/container}
         [quo/drawer-top
          {:type                :context-tag
           :title               (i18n/label :t/addresses-for-permissions)
           :context-tag-type    :community
           :community-name      name
           :button-icon         :i/info
           :on-button-press     not-implemented/alert
           :community-logo      (get-in images [:thumbnail :uri])
           :customization-color color}]

         [quo/category
          {:list-type       :settings
           :data            [{:title        (i18n/label :t/share-all-current-and-future-addresses)
                              :action       :selector
                              :action-props {:on-change #(rf/dispatch
                                                          [:communities/toggle-share-all-addresses
                                                           id])
                                             :customization-color color
                                             :checked? share-all-addresses?}}]
           :container-style {:padding-bottom 16}}]

         [gesture/flat-list
          {:render-fn               account-item
           :render-data             {:selected-addresses   selected-addresses
                                     :community-id         id
                                     :share-all-addresses? share-all-addresses?
                                     :community-color      color}
           :content-container-style {:padding-horizontal 20}
           :scroll-enabled          scroll-enabled?
           :on-scroll               on-scroll
           :key-fn                  :address
           :data                    accounts}]

         (if (and highest-permission-role (seq selected-addresses))
           [rn/view
            {:style style/highest-role}
            [quo/text
             {:size  :paragraph-2
              :style {:color colors/neutral-50}}
             (i18n/label :t/eligible-to-join-as)]
            [quo/context-tag
             {:type    :icon
              :icon    :i/members
              :size    24
              :context highest-role-text}]]
           [quo/info-message
            {:type  :error
             :size  :default
             :icon  :i/info
             :style {:justify-content :center}}
            (if (empty? selected-addresses)
              (i18n/label :t/no-addresses-selected)
              (i18n/label :t/addresses-dont-contain-tokens-needed))])

         [rn/view {:style style/buttons}
          [quo/button
           {:type            :grey
            :container-style {:flex 1}
            :on-press        (fn []
                               (rf/dispatch [:communities/reset-selected-permission-addresses id])
                               (rf/dispatch [:navigate-back]))}
           (i18n/label :t/cancel)]
          [quo/button
           {:container-style     {:flex 1}
            :customization-color color
            :disabled?           (or (empty? selected-addresses)
                                     (not highest-permission-role)
                                     (not unsaved-address-changes?))
            :on-press            (fn []
                                   (rf/dispatch [:communities/update-previous-permission-addresses id])
                                   (rf/dispatch [:navigate-back]))}
           (i18n/label :t/confirm-changes)]]]))))
