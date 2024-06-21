(ns status-im.contexts.wallet.connected-dapps.disconnect-dapp.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.connected-dapps.disconnect-dapp.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [customization-color dapp on-disconnect]}]
  (let [{:keys [avatar name]} dapp]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :full-name           name
       :profile-picture     avatar
       :title               (i18n/label :t/disconnect-dapp)
       :customization-color customization-color}]
     [rn/view {:style style/content-wrapper}
      [quo/text
       {:size :paragraph-1}
       (i18n/label :t/disconnect-dapp-confirmation {:dapp name})]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/disconnect)
       :button-one-props {:type                :danger
                          :accessibility-label :block-contact
                          :on-press            on-disconnect}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type                :grey
                          :accessibility-label :cancel
                          :on-press            #(rf/dispatch [:hide-bottom-sheet])}}]]))
