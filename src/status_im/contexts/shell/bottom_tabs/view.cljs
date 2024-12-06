(ns status-im.contexts.shell.bottom-tabs.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [status-im.config :as config]
    [status-im.contexts.shell.bottom-tabs.style :as style]
    [status-im.contexts.shell.constants :as shell.constants]
    [utils.re-frame :as rf]))

(defn bottom-tab
  [icon stack-id shared-values notifications-data]
  (let [customization-color (rf/sub [:profile/customization-color])
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
                :accessibility-label (str (name stack-id) "-tab")
                :customization-color customization-color))]))

(defn view
  [shared-values]
  (let [notifications-data             (rf/sub [:shell/bottom-tabs-notifications-data])
        communities-double-tab-gesture (-> (gesture/gesture-tap)
                                           (gesture/number-of-taps 2)
                                           (gesture/on-start
                                            (fn [_event]
                                              (rf/dispatch [:communities/select-tab :joined]))))
        messages-double-tap-gesture    (-> (gesture/gesture-tap)
                                           (gesture/number-of-taps 2)
                                           (gesture/on-start
                                            (fn [_event]
                                              (rf/dispatch [:messages-home/select-tab :tab/recent]))))]
    [quo.theme/provider :dark
     [reanimated/view
      {:style (style/bottom-tabs-container (:bottom-tabs-height shared-values))}
      [rn/view {:style (style/bottom-tabs)}
       [bottom-tab :i/wallet :wallet-stack shared-values notifications-data]
       [gesture/gesture-detector {:gesture messages-double-tap-gesture}
        [bottom-tab :i/messages :chats-stack shared-values notifications-data]]
       [gesture/gesture-detector {:gesture communities-double-tab-gesture}
        [bottom-tab :i/communities :communities-stack shared-values notifications-data]]
       (when config/show-not-implemented-features?
         [bottom-tab :i/browser :browser-stack shared-values notifications-data])]]]))
