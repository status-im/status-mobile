(ns status-im.contexts.profile.contact.view
  (:require [quo.foundations.colors :as colors]
            [quo.theme]
            [react-native.reanimated :as reanimated]
            [status-im.common.scroll-page.view :as scroll-page]
            [status-im.contexts.profile.contact.actions.view :as actions]
            [status-im.contexts.profile.contact.header.view :as contact-header]
            [utils.re-frame :as rf]))

(defn on-show-actions
  []
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [actions/view])}]))

(defn view
  []
  (let [{:keys [customization-color]} (rf/sub [:contacts/current-contact])
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
      [contact-header/view {:scroll-y scroll-y}]]]))
