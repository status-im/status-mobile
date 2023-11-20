(ns status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.constants :as constants]
            [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [margin-top? page-nav-right-side account-name account-color account-emoji on-change-name
           on-change-color
           on-change-emoji on-focus on-blur section-label bottom-action?
           bottom-action-label bottom-action-props
           custom-bottom-action watch-only?]} & children]
  (let [{:keys [top bottom]}  (safe-area/get-insets)
        margin-top            (if (false? margin-top?) 0 top)
        {window-width :width} (rn/get-window)]
    [rn/keyboard-avoiding-view
     {:style                    (style/root-container margin-top)
      :keyboard-vertical-offset (- bottom)}
     [quo/page-nav
      {:type       :no-title
       :background :blur
       :right-side page-nav-right-side
       :icon-name  :i/close
       :on-press   #(rf/dispatch [:navigate-back])}]
     [quo/gradient-cover
      {:customization-color account-color
       :container-style     (style/gradient-cover-container margin-top)}]
     (into
      [rn/scroll-view {:bounces false}
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
        {:placeholder     (i18n/label :t/account-name-input-placeholder)
         :max-length      constants/wallet-account-name-max-length
         :blur?           true
         :default-value   account-name
         :on-change-text  on-change-name
         :container-style style/title-input-container
         :on-focus        on-focus
         :on-blur         on-blur}]
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
           :container-style style/section-container}])]
      children)
     (when bottom-action?
       [rn/view {:style (style/bottom-action bottom)}
        (if custom-bottom-action
          custom-bottom-action
          [quo/button
           (merge
            {:size 40
             :type :primary}
            bottom-action-props)
           (i18n/label bottom-action-label)])])]))
