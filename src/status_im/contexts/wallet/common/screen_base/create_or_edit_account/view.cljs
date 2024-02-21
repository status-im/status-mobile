(ns status-im.contexts.wallet.common.screen-base.create-or-edit-account.view
  (:require [quo.core :as quo]
            quo.theme
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.screen-base.create-or-edit-account.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view-internal
  [{:keys [margin-top? page-nav-right-side placeholder account-name account-color account-emoji
           on-change-name
           on-change-color
           on-change-emoji section-label
           bottom-action-label bottom-action-props
           custom-bottom-action watch-only? theme]} & children]
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
      [rn/scroll-view
       {:keyboard-should-persist-taps :always
        :style                        {:flex 1}}
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
           :container-style style/section-container}])]
      children)
     [rn/view
      {:style (style/bottom-action {:theme  theme
                                    :bottom bottom})}
      (if custom-bottom-action
        custom-bottom-action
        [quo/button
         (merge
          {:size 40
           :type :primary}
          bottom-action-props)
         (i18n/label bottom-action-label)])]]))

(def view (quo.theme/with-theme view-internal))
