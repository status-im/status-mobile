(ns status-im.contexts.wallet.common.screen-base.create-or-edit-account.view
  (:require [quo.core :as quo]
            quo.theme
            [react-native.core :as rn]
            [status-im.common.floating-button-page.view :as floating-button-page]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.screen-base.create-or-edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [page-nav-right-side placeholder account-name account-color account-emoji
           on-change-name
           on-change-color
           on-change-emoji section-label
           bottom-action-label bottom-action-props
           custom-bottom-action watch-only?]} & children]
  (let [{window-width :width} (rn/get-window)]
    [floating-button-page/view
     {:header                   [quo/page-nav
                                 {:type       :no-title
                                  :background :blur
                                  :right-side page-nav-right-side
                                  :icon-name  :i/close
                                  :on-press   #(rf/dispatch [:navigate-back])}]
      :footer                   (if custom-bottom-action
                                  custom-bottom-action
                                  [quo/button
                                   (merge
                                    {:size 40
                                     :type :primary}
                                    bottom-action-props)
                                   (i18n/label bottom-action-label)])
      :gradient-cover?          true
      :footer-container-padding 0
      :customization-color      account-color}
     [:<>
      [rn/view {:style style/account-avatar-container}
       [quo/account-avatar
        {:customization-color account-color
         :size                80
         :emoji               account-emoji
         :type                (if watch-only? :watch-only :default)}]
       [quo/button
        {:size            32
         :type            :grey
         :background      :photo
         :icon-only?      true
         :on-press        #(rf/dispatch [:emoji-picker/open {:on-select on-change-emoji}])
         :container-style style/reaction-button-container}
        :i/reaction]]
      [quo/title-input
       {:placeholder     placeholder
        :max-length      constants/wallet-account-name-max-length
        :blur?           true
        :default-value   account-name
        :auto-focus      true
        :on-change-text  on-change-name
        :container-style style/title-input-container}]
      [quo/divider-line {:container-style style/divider-1}]
      [quo/section-label
       {:section         (i18n/label :t/colour)
        :container-style style/section-container}]
      [rn/view
       {:style style/color-picker-container}
       [quo/color-picker
        {:default-selected account-color
         :on-change        on-change-color
         :container-style  style/color-picker
         :window-width     window-width}]]
      [quo/divider-line {:container-style style/divider-2}]
      (when section-label
        [quo/section-label
         {:section         (i18n/label section-label)
          :container-style style/section-container}])
      children]]))
