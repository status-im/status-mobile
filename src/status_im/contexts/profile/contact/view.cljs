(ns status-im.contexts.profile.contact.view
  (:require [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.reanimated :as reanimated]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.common.scroll-page.view :as scroll-page]
            [status-im.contexts.profile.contact.header.view :as contact-header]
            [utils.re-frame :as rf]))

(defn scroll-handler
  [current-y scroll-y]
  (println current-y)
  (reanimated/set-shared-value scroll-y current-y))

(defn- f-view
  [{:keys [theme]}]
  (let [{:keys [customization-color]} (rf/sub [:contacts/current-contact])
        scroll-y                      (reanimated/use-shared-value 0)]
    [scroll-page/scroll-page
     {:navigate-back?   true
      :height           148
      :on-scroll        #(reanimated/set-shared-value scroll-y %)
      ;TODO remove colors/primary-50 when we have contact accent color
      :cover-color      (or customization-color colors/primary-50)
      :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
      :page-nav-props   {:right-side [{:icon-name :i/options
                                       :on-press  not-implemented/alert}]}}
     [contact-header/view {:scroll-y scroll-y}]]))

(def view (quo.theme/with-theme f-view))
