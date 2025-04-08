(ns status-im.contexts.profile.settings.view
  (:require [oops.core :as oops]
            [quo.context]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.profile.settings.header.view :as settings.header]
            [status-im.contexts.profile.settings.list-items :as settings.items]
            [status-im.contexts.profile.settings.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [status-im.feature-flags :as ff]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(defn show-settings-item?
  [{:keys [feature-flag]}]
  (or (ff/enabled? feature-flag)
      (nil? feature-flag)))

(defn- settings-category-view
  [data]
  (rn/delay-render
   [quo/category
    {:list-type       :settings
     :container-style {:padding-top    8
                       :padding-bottom 4}
     :blur?           true
     :data            (doall (filter show-settings-item? data))}]))

(defn scroll-handler
  [event scroll-y]
  (let [current-y (oops/oget event "nativeEvent.contentOffset.y")]
    (reanimated/set-shared-value scroll-y current-y)))

(defn- logout-press
  []
  (rf/dispatch [:profile.settings/ask-logout]))

(defn- footer
  [{:keys [bottom]}]
  (rn/delay-render
   (let [logging-out? (rf/sub [:profile/logging-out?])]
     [rn/view {:style (style/footer-container bottom)}
      [quo/logout-button
       {:on-press  logout-press
        :disabled? logging-out?}]])))

(defn- get-item-layout
  [_ index]
  #js {:length 100 :offset (* 100 index) :index index})

(defn- navigate-to-backup-challenge
  [masked-seed-phrase]
  (rf/dispatch
   [:open-modal :screen/confirm-backup-on-shell
    {:masked-seed-phrase masked-seed-phrase
     :on-try-again       (fn []) ;; Needed due to impl. checks
     :on-success         (fn []
                           (rf/dispatch [:dismiss-modal :screen/backup-recovery-phrase-on-shell])
                           (rf/dispatch [:dismiss-modal :screen/confirm-backup-on-shell])
                           (rf/dispatch [:my-profile/finish]))
     :back-button?       true
     :shell?             true}]))

(defn navigate-to-backup-seed
  [mnemonic]
  (fn []
    (rf/dispatch
     [:open-modal
      :screen/backup-recovery-phrase-on-shell
      {:back-button?       true
       :shell?             true
       :masked-seed-phrase (security/mask-data mnemonic)
       :on-success         navigate-to-backup-challenge}])))

(defn view
  []
  (let [theme                (quo.context/use-theme)
        insets               safe-area/insets
        customization-color  (rf/sub [:profile/customization-color])
        scroll-y             (reanimated/use-shared-value 0)
        profile              (rf/sub [:profile/profile])
        mnemonic             (rf/sub [:profile/mnemonic])
        full-name            (profile.utils/displayed-name profile)
        on-scroll            (rn/use-callback #(scroll-handler % scroll-y))
        on-backup-seed-press (rn/use-callback #(navigate-to-backup-seed mnemonic))]
    [quo/overlay {:type :shell}
     [rn/view
      {:style (style/navigation-wrapper {:customization-color customization-color
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
                      :on-press  #(debounce/throttle-and-dispatch [:open-modal :screen/share-shell
                                                                   {:initial-tab :profile}]
                                                                  1000)}
                     {:icon-name :i/share
                      :on-press  #(rf/dispatch [:open-share
                                                {:options {:message (:universal-profile-url
                                                                     profile)}}])}]}]]
     [rn/flat-list
      {:header                          [settings.header/view {:scroll-y scroll-y}]
       :data                            (settings.items/items mnemonic on-backup-seed-press)
       :shows-vertical-scroll-indicator false
       :render-fn                       settings-category-view
       :get-item-layout                 get-item-layout
       :footer                          [footer insets]
       :scroll-event-throttle           16
       :on-scroll                       on-scroll
       :bounces                         false
       :over-scroll-mode                :never}]]))
