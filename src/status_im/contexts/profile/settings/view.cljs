(ns status-im.contexts.profile.settings.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.profile.settings.header.view :as settings.header]
            [status-im.contexts.profile.settings.list-items :as settings.items]
            [status-im.contexts.profile.settings.style :as style]
            [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- settings-item-view
  [data]
  [rf/delay-render
   [quo/category
    {:list-type :settings
     :style     {:padding-bottom 0}
     :blur?     true
     :data      data}]])

(defn scroll-handler
  [event scroll-y]
  (let [current-y (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y current-y)))

(defn- footer
  [logout-press]
  [rf/delay-render
   [rn/view {:style style/footer-container}
    [quo/logout-button {:on-press logout-press}]]])

(defn- get-item-layout
  [_ index]
  #js {:length 100 :offset (* 100 index) :index index})

(defn- settings-view
  [theme]
  (let [insets              (safe-area/get-insets)
        customization-color (rf/sub [:profile/customization-color])
        scroll-y            (reanimated/use-shared-value 0)
        logout-press        #(rf/dispatch [:multiaccounts.logout.ui/logout-pressed])]
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :header
       :style (style/navigation-wrapper {:customization-color customization-color
                                         :inset               (:top insets)
                                         :theme               theme})}
      [quo/page-nav
       {:background :blur
        :icon-name  :i/close
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/multi-profile :on-press #(rf/dispatch [:open-modal :sign-in])}
                     {:icon-name :i/qr-code
                      :on-press  #(debounce/dispatch-and-chill [:open-modal :share-shell] 1000)}
                     {:icon-name :i/share :on-press not-implemented/alert}]}]]
     [rn/flat-list
      {:key                             :list
       :header                          [settings.header/view {:scroll-y scroll-y}]
       :data                            settings.items/items
       :shows-vertical-scroll-indicator false
       :render-fn                       settings-item-view
       :get-item-layout                 get-item-layout
       :footer                          [footer logout-press]
       :scroll-event-throttle           16
       :on-scroll                       #(scroll-handler % scroll-y)
       :bounces                         false}]
     [quo/floating-shell-button
      {:key :shell
       :jump-to
       {:on-press            (fn []
                               (rf/dispatch [:navigate-back])
                               (debounce/dispatch-and-chill [:shell/navigate-to-jump-to] 500))
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   jump-to.constants/floating-shell-button-height}]]))

(defn- internal-view
  [props]
  [:f> settings-view props])

(def view (quo.theme/with-theme internal-view))
