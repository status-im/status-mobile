(ns status-im.contexts.profile.settings.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.profile.settings.header.view :as settings.header]
            [status-im.contexts.profile.settings.list-items :as settings.items]
            [status-im.contexts.profile.settings.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- settings-item-view
  [data]
  [rf/delay-render
   [quo/category
    {:list-type       :settings
     :container-style {:padding-bottom 12}
     :blur?           true
     :data            data}]])

(defn scroll-handler
  [event scroll-y]
  (let [current-y (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y current-y)))

(defn- footer
  [{:keys [bottom]} logout-press]
  [rf/delay-render
   [rn/view {:style (style/footer-container bottom)}
    [quo/logout-button {:on-press logout-press}]]])

(defn- get-item-layout
  [_ index]
  #js {:length 100 :offset (* 100 index) :index index})

(defn view
  []
  (let [theme               (quo.theme/use-theme)
        insets              (safe-area/get-insets)
        customization-color (rf/sub [:profile/customization-color])
        scroll-y            (reanimated/use-shared-value 0)
        logout-press        #(rf/dispatch [:multiaccounts.logout.ui/logout-pressed])
        profile             (rf/sub [:profile/profile])
        full-name           (profile.utils/displayed-name profile)]
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :header
       :style (style/navigation-wrapper {:customization-color customization-color
                                         :inset               (:top insets)
                                         :theme               theme})}
      [quo/page-nav
       {:title      full-name
        :background :blur
        :type       :title
        :text-align :left
        :scroll-y   scroll-y
        :icon-name  :i/close
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/qr-code
                      :on-press  #(debounce/throttle-and-dispatch [:open-modal :share-shell] 1000)}
                     {:icon-name :i/share
                      :on-press  #(rf/dispatch [:open-share
                                                {:options {:message (:universal-profile-url
                                                                     profile)}}])}]}]]
     [rn/flat-list
      {:key                             :list
       :header                          [settings.header/view {:scroll-y scroll-y}]
       :data                            settings.items/items
       :shows-vertical-scroll-indicator false
       :render-fn                       settings-item-view
       :get-item-layout                 get-item-layout
       :footer                          [footer insets logout-press]
       :scroll-event-throttle           16
       :on-scroll                       #(scroll-handler % scroll-y)
       :bounces                         false
       :over-scroll-mode                :never}]
     [quo/floating-shell-button
      {:key :shell
       :jump-to
       {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      (style/floating-shell-button-style insets)]]))
