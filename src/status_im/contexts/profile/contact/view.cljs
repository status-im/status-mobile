(ns status-im.contexts.profile.contact.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.reanimated :as reanimated]
            [status-im.common.scroll-page.view :as scroll-page]
            [status-im.contexts.profile.contact.actions.view :as actions]
            [status-im.contexts.profile.contact.header.view :as contact-header]
            [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
            [status-im.feature-flags :as ff]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-show-actions
  []
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [actions/view])}]))

(defn on-jump-to
  []
  (rf/dispatch [:navigate-back])
  (debounce/throttle-and-dispatch [:shell/navigate-to-jump-to] 500))

(defn view
  []
  (let [{:keys [customization-color]} (rf/sub [:contacts/current-contact])
        profile-customization-color   (rf/sub [:profile/customization-color])
        scroll-y                      (reanimated/use-shared-value 0)
        theme                         (quo.theme/use-theme)]
    [:<>
     [scroll-page/scroll-page
      {:navigate-back?   true
       :height           148
       :on-scroll        #(reanimated/set-shared-value scroll-y %)
       :cover-color      (colors/resolve-color customization-color
                                               theme
                                               20)
       :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
       :page-nav-props   {:right-side [{:icon-name           :i/options
                                        :on-press            on-show-actions
                                        :accessibility-label :contact-actions}]}}
      [contact-header/view {:scroll-y scroll-y}]]
     (when (ff/enabled? ::ff/shell.jump-to)
       [quo/floating-shell-button
        {:jump-to
         {:on-press            on-jump-to
          :customization-color profile-customization-color
          :label               (i18n/label :t/jump-to)}}
        {:position :absolute
         :bottom   jump-to.constants/floating-shell-button-height}])]))
