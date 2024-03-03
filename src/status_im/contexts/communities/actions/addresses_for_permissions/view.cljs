(ns status-im.contexts.communities.actions.addresses-for-permissions.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.constants :as constants]
            [status-im.contexts.communities.actions.addresses-for-permissions.style :as style]
            [utils.i18n :as i18n]
            [utils.money :as money]
            [utils.re-frame :as rf]))

(defn- role-keyword
  [role]
  (condp = role
    constants/community-token-permission-become-token-owner  :token-owner
    constants/community-token-permission-become-token-master :token-master
    constants/community-token-permission-become-admin        :admin
    constants/community-token-permission-become-member       :member
    nil))

(defn- balances->components-props
  [balances token-images-map]
  (for [{:keys [amount decimals type name] sym :symbol :as balance} balances]
    (cond-> balance
      true
      (assoc :type
             (condp = type
               constants/community-token-type-erc20  :token
               constants/community-token-type-erc721 :collectible
               :token))

      (= type constants/community-token-type-erc721)
      (assoc :collectible-name    name
             :collectible-img-src (token-images-map sym))

      (= type constants/community-token-type-erc20)
      (assoc :amount        (str (money/token->unit amount decimals))
             :token         (:symbol balance)
             :token-img-src (token-images-map sym)))))

(defn- account-item
  [{:keys [color address name emoji]} _ _
   {:keys [selected-addresses community-id share-all-addresses? community-color]}]
  (let [balances         (rf/sub [:communities/permissioned-balances-by-address community-id address])
        token-images-map (rf/sub [:communities/token-requirements-images community-id])]
    [quo/account-permissions
     {:account             {:name                name
                            :address             address
                            :emoji               emoji
                            :customization-color color}
      :token-details       (balances->components-props balances token-images-map)
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
            {:keys [checking?
                    highest-permission-role]} (rf/sub [:community/token-gated-overview id])
            accounts                          (rf/sub [:wallet/accounts-without-watched-accounts])
            selected-addresses                (rf/sub [:communities/selected-permission-addresses id])
            share-all-addresses?              (rf/sub [:communities/share-all-addresses? id])
            unsaved-address-changes?          (rf/sub [:communities/unsaved-address-changes? id])]
        [rn/safe-area-view {:style style/container}
         [quo/drawer-top
          {:type                :context-tag
           :title               (i18n/label :t/addresses-for-permissions)
           :context-tag-type    :community
           :community-name      name
           :button-icon         :i/info
           :button-type         :grey
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
           :scroll-enabled          @scroll-enabled?
           :on-scroll               on-scroll
           :key-fn                  :address
           :data                    accounts}]

         [quo/bottom-actions
          {:actions          :two-actions
           :button-one-label (i18n/label :t/confirm-changes)
           :button-one-props {:customization-color color
                              :disabled?           (or checking?
                                                       (empty? selected-addresses)
                                                       (not highest-permission-role)
                                                       (not unsaved-address-changes?))
                              :on-press            (fn []
                                                     (rf/dispatch
                                                      [:communities/update-previous-permission-addresses
                                                       id])
                                                     (rf/dispatch [:navigate-back]))}
           :button-two-label (i18n/label :t/cancel)
           :button-two-props {:type     :grey
                              :on-press (fn []
                                          (rf/dispatch
                                           [:communities/reset-selected-permission-addresses id])
                                          (rf/dispatch [:navigate-back]))}
           :description      (if (or (empty? selected-addresses)
                                     (not highest-permission-role))
                               :top-error
                               :top)
           :role             (when-not checking? (role-keyword highest-permission-role))
           :error-message    (cond
                               (empty? selected-addresses)   (i18n/label :t/no-addresses-selected)
                               (not highest-permission-role) (i18n/label
                                                              :t/addresses-dont-contain-tokens-needed)
                               :else                         nil)}]]))))

