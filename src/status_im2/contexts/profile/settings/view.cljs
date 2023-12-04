(ns status-im2.contexts.profile.settings.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.contexts.profile.settings.header.view :as settings.header]
            [status-im2.contexts.profile.settings.list-items :as settings.items]
            [status-im2.contexts.profile.settings.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- settings-item-view
  [data]
  [quo/category
   {:list-type :settings
    :blur?     true
    :data      data}])

(defn scroll-handler
  [event scroll-y]
  (let [current-y (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y current-y)))

(defn- footer
  [logout-press]
  [rn/view {:style style/footer-container}
   [quo/button
    {:on-press  logout-press
     :type      :danger
     :icon-left :i/log-out}
    (i18n/label :t/logout)]])

(defn- settings-view
  [theme]
  (let [insets              (safe-area/get-insets)
        customization-color (rf/sub [:profile/customization-color])
        scroll-y            (reanimated/use-shared-value 0)
        logout-press        #(rf/dispatch [:multiaccounts.logout.ui/logout-pressed])]
    [quo/overlay {:type :shell}
     [rn/view
      {:style (style/navigation-wrapper {:customization-color customization-color
                                         :inset               (:top insets)
                                         :theme               theme})}
      [quo/page-nav
       {:background :blur
        :icon-name  :i/close
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/multi-profile :on-press #(rf/dispatch [:open-modal :sign-in])}
                     {:icon-name :i/qr-code :on-press not-implemented/alert}
                     {:icon-name :i/share
                      :on-press  #(debounce/dispatch-and-chill [:open-modal :share-shell] 1000)}]}]]
     [rn/flat-list
      {:header                          [settings.header/view {:scroll-y scroll-y}]
       :data                            settings.items/items
       :key-fn                          :title
       :shows-vertical-scroll-indicator false
       :render-fn                       settings-item-view
       :footer                          [footer logout-press]
       :scroll-event-throttle           16
       :on-scroll                       #(scroll-handler % scroll-y)
       :bounces                         false}]]))

(defn- internal-view
  [props]
  [:f> settings-view props])

(def view (quo.theme/with-theme internal-view))
