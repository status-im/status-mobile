(ns status-im.contexts.profile.contact.view
  (:require [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.common.scroll-page.view :as scroll-page]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.actions.view :as actions]
            [status-im.contexts.profile.contact.header.view :as contact-header]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [customization-color]} (rf/sub [:contacts/current-contact])
        scroll-y                      (reanimated/use-shared-value 0)
        theme                         (quo.theme/use-theme-value)
        on-action-press               (rn/use-callback (fn []
                                                         (rf/dispatch [:show-bottom-sheet
                                                                       {:content (fn []
                                                                                   [actions/view])}])))]
    [scroll-page/scroll-page
     {:navigate-back?   true
      :height           148
      :on-scroll        #(reanimated/set-shared-value scroll-y %)
      ;; TODO(@mohsen): remove default color, https://github.com/status-im/status-mobile/issues/18733
      :cover-color      (or customization-color constants/profile-default-color)
      :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
      :page-nav-props   {:right-side [{:icon-name :i/options
                                       :on-press  on-action-press}]}}
     [contact-header/view {:scroll-y scroll-y}]]))
