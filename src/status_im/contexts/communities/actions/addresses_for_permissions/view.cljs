(ns status-im.contexts.communities.actions.addresses-for-permissions.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.communities.actions.addresses-for-permissions.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- account-item
  [item]
  [rn/view
   {:style style/account-item-container}
   [quo/account-avatar (assoc item :size 32)]
   [rn/view
    [quo/text
     {:size   :paragraph-1
      :weight :semi-bold}
     (:name item)]
    [quo/address-text
     {:address (:address item)
      :format  :short}]]])

(defn view
  []
  (let [{id :community-id}          (rf/sub [:get-screen-params])
        {:keys [name color images]} (rf/sub [:communities/community id])
        accounts                    (rf/sub [:wallet/accounts-with-customization-color])]
    [rn/safe-area-view {:style style/container}

     [quo/drawer-top
      {:type                :context-tag
       :title               (i18n/label :t/addresses-for-permissions)
       :community-name      name
       :button-icon         :i/info
       :on-button-press     not-implemented/alert
       :community-logo      (get-in images [:thumbnail :uri])
       :customization-color color}]

     [rn/flat-list
      {:render-fn               account-item
       :content-container-style {:padding 20}
       :key-fn                  :address
       :data                    accounts}]

     [rn/view {:style style/buttons}
      [quo/button
       {:type            :grey
        :container-style {:flex 1}
        :on-press        #(rf/dispatch [:navigate-back])}
       (i18n/label :t/cancel)]
      [quo/button
       {:container-style     {:flex 1}
        :customization-color color
        :on-press            #(rf/dispatch [:navigate-back])}
       (i18n/label :t/confirm-changes)]]]))
