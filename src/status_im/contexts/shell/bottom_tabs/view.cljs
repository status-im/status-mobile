(ns status-im.contexts.shell.bottom-tabs.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [status-im.config :as config]
    [status-im.contexts.shell.bottom-tabs.style :as style]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.feature-flags :as ff]
    [utils.re-frame :as rf]))

(defn bottom-tab
  [icon stack-id shared-values privacy-mode-enabled?]
  (let [notifications-data  (rf/sub [:shell/bottom-tabs-notifications-data])
        customization-color (rf/sub [:profile/customization-color])
        on-press            (rn/use-callback #(rf/dispatch [:shell/change-tab stack-id]))
        icon-color          (->> stack-id
                                 (get shell.constants/tabs-icon-color-keywords)
                                 (get shared-values))]
    [quo/bottom-nav-tab
     (-> notifications-data
         (get stack-id)
         (assoc :test-ID             stack-id
                :icon                icon
                :icon-color-anim     icon-color
                :on-press            on-press
                :pass-through?       privacy-mode-enabled?
                :accessibility-label (str (name stack-id) "-tab")
                :customization-color customization-color))]))

(defn view
  [shared-values privacy-mode-enabled?]
  (let [communities-double-tab-gesture (-> (gesture/gesture-tap)
                                           (gesture/number-of-taps 2)
                                           (gesture/on-start
                                            (fn [_event]
                                              (rf/dispatch [:communities/select-tab :joined]))))
        messages-double-tap-gesture    (-> (gesture/gesture-tap)
                                           (gesture/number-of-taps 2)
                                           (gesture/on-start
                                            (fn [_event]
                                              (rf/dispatch [:messages-home/select-tab :tab/recent]))))]
    [quo.context/provider {:theme :dark}
     [reanimated/view
      {:style (style/bottom-tabs-container privacy-mode-enabled?)}
      [rn/view {:style (style/bottom-tabs)}
       [bottom-tab :i/wallet :screen/wallet-stack shared-values privacy-mode-enabled?]
       (when (ff/enabled? ::ff/market)
         [bottom-tab :i/swap :screen/market-stack shared-values privacy-mode-enabled?])
       [gesture/gesture-detector {:gesture messages-double-tap-gesture}
        [bottom-tab :i/messages :screen/chats-stack shared-values privacy-mode-enabled?]]
       [gesture/gesture-detector {:gesture communities-double-tab-gesture}
        [bottom-tab :i/communities :screen/communities-stack shared-values privacy-mode-enabled?]]
       (when config/show-not-implemented-features?
         [bottom-tab :i/browser :screen/browser-stack shared-values privacy-mode-enabled?])]]]))
