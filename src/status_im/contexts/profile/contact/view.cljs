(ns status-im.contexts.profile.contact.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.common.scroll-page.view :as scroll-page]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.actions.view :as actions]
            [status-im.contexts.profile.contact.header.view :as contact-header]
            [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
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
        scroll-y                      (reanimated/use-shared-value 0)
        theme                         (quo.theme/use-theme-value)]
    [:<>
     [scroll-page/scroll-page
      {:navigate-back?   true
       :height           148
       :on-scroll        #(reanimated/set-shared-value scroll-y %)
       ;; TODO(@mohsen): remove default color, https://github.com/status-im/status-mobile/issues/18733
       :cover-color      (colors/resolve-color (or customization-color constants/profile-default-color)
                                               theme
                                               20)
       :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
       :page-nav-props   {:right-side [{:icon-name           :i/options
                                        :on-press            on-show-actions
                                        :accessibility-label :contact-actions}]}}
      [contact-header/view {:scroll-y scroll-y}]]
     [quo/floating-shell-button
      {:jump-to
       {:on-press            on-jump-to
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   jump-to.constants/floating-shell-button-height}]]))
