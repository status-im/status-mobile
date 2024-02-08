(ns status-im.contexts.communities.actions.addresses-for-permissions.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.communities.actions.addresses-for-permissions.style :as style]
            [status-im.contexts.communities.utils :as communities.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- account-item
  [item _ _ [selected-addresses community-id]]
  [quo/account-permissions
   {:account         {:name                (:name item)
                      :address             (:address item)
                      :emoji               (:emoji item)
                      :customization-color (:customization-color item)}
    :token-details   []
    :checked?        (contains? selected-addresses (:address item))
    :on-change       #(rf/dispatch [:communities/toggle-selected-permission-address
                                    (:address item) community-id])
    :container-style {:margin-bottom 8}}])

(defn view
  []
  (let [{id :community-id} (rf/sub [:get-screen-params])
        {:keys [name color images]} (rf/sub [:communities/community id])
        {:keys [highest-permission-role]} (rf/sub [:community/token-gated-overview id])
        accounts (rf/sub [:wallet/accounts-with-customization-color])
        selected-addresses (rf/sub [:communities/selected-permission-addresses id])
        highest-role-text
        (i18n/label
         (communities.utils/role->translation-key highest-permission-role))]
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

     [rn/flat-list
      {:render-fn               account-item
       :render-data             [selected-addresses id]
       :content-container-style {:padding 20}
       :key-fn                  :address
       :data                    accounts}]

     (when (and highest-permission-role (seq selected-addresses))
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
          :context highest-role-text}]])

     (when (empty? selected-addresses)
       [rn/view
        {:style style/error-message}
        [quo/icon
         :i/info
         {:color colors/danger-50
          :size  16}]
        [quo/text
         {:size  :paragraph-2
          :style {:color colors/danger-50}}
         (i18n/label :t/no-addresses-selected)]])

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
        :disabled?           (empty? selected-addresses)
        :on-press            (fn []
                               (rf/dispatch [:communities/update-previous-permission-addresses id])
                               (rf/dispatch [:navigate-back]))}
       (i18n/label :t/confirm-changes)]]]))
