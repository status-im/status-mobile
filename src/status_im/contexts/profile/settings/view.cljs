(ns status-im.contexts.profile.settings.view
  (:require [oops.core :as oops]
            [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.pure :as rn.pure]
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
  [^js data]
  (quo/category
   {:list-type       :settings
    :container-style {:padding-bottom 0}
    :blur?           true
    :data            (.-item data)}))

(defn scroll-handler
  [scroll-y event]
  (let [current-y (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y current-y)))

(defn- footer
  []
  (rn.pure/view
   {:style style/footer-container}
   (quo/logout-button {:on-press #(rf/dispatch [:multiaccounts.logout.ui/logout-pressed])})))

(defn- settings-view
  []
  (let [insets              (safe-area/get-insets)
        theme               (quo.theme/use-theme)
        customization-color (rf/use-subscription [:profile/customization-color])
        scroll-y            (reanimated/use-shared-value 0)
        on-scroll           (partial scroll-handler scroll-y)
        header              (partial settings.header/view {:scroll-y scroll-y})
        key-extractor       #(:title (first %1))]
    (quo/overlay
     {:type :shell}
     (rn.pure/view
      {:key   :header
       :style (style/navigation-wrapper {:customization-color customization-color
                                         :inset               (:top insets)
                                         :theme               theme})}
      (quo/page-nav
       {:background :blur
        :icon-name  :i/close
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/multi-profile :on-press #(rf/dispatch [:open-modal :sign-in])}
                     {:icon-name :i/qr-code
                      :on-press  #(debounce/dispatch-and-chill [:open-modal :share-shell] 1000)}
                     {:icon-name :i/share :on-press not-implemented/alert}]}))
     (rn.pure/flat-list
      #js
       {:key                          "settings-flat-list"
        :keyExtractor                 key-extractor
        :ListHeaderComponent          header
        :data                         settings.items/items
        :showsVerticalScrollIndicator false
        :renderItem                   settings-item-view
        :ListFooterComponent          footer
        :scrollEventThrottle          16
        :onScroll                     on-scroll
        :bounces                      false})
     (quo/floating-shell-button
      {:key :shell
       :jump-to
       {:on-press            (fn []
                               (rf/dispatch [:navigate-back])
                               (debounce/dispatch-and-chill [:shell/navigate-to-jump-to] 500))
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   jump-to.constants/floating-shell-button-height}))))

(defn view
  []
  (rn.pure/func settings-view))
