(ns status-im2.contexts.shell.jump-to.components.bottom-tabs.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [quo2.core :as quo]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [status-im2.contexts.shell.jump-to.state :as state]
            [status-im2.contexts.shell.jump-to.animation :as animation]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]
            [status-im2.contexts.shell.jump-to.components.bottom-tabs.style :as style]))

(defn blur-overlay-params
  [style]
  {:style         style
   :blur-amount   (if platform/android? 30 20)
   :blur-radius   (if platform/android? 25 10)
   :blur-type     :transparent
   :overlay-color :transparent})

(defn bottom-tab
  [icon stack-id shared-values notifications-data]
  (let [customization-color (rf/sub [:profile/customization-color])
        icon-color          (->> stack-id
                                 (get shell.constants/tabs-icon-color-keywords)
                                 (get shared-values))]
    [quo/bottom-nav-tab
     (-> notifications-data
         (get stack-id)
         (assoc :test-ID             stack-id
                :icon                icon
                :icon-color-anim     icon-color
                ;NOTE temporary use of on long press while we support old wallet
                :on-long-press       #(when (= stack-id :wallet-stack)
                                        (swap! state/load-new-wallet? not)
                                        (animation/bottom-tab-on-press stack-id true))
                :on-press            #(animation/bottom-tab-on-press stack-id true)
                :accessibility-label (str (name stack-id) "-tab")
                :customization-color customization-color))]))

(defn f-bottom-tabs
  []
  (let [notifications-data             (rf/sub [:shell/bottom-tabs-notifications-data])
        pass-through?                  (rf/sub [:shell/shell-pass-through?])
        shared-values                  @state/shared-values-atom
        communities-double-tab-gesture (-> (gesture/gesture-tap)
                                           (gesture/number-of-taps 2)
                                           (gesture/on-start
                                            (fn [_event]
                                              (rf/dispatch [:communities/select-tab :joined]))))
        messages-double-tap-gesture    (-> (gesture/gesture-tap)
                                           (gesture/number-of-taps 2)
                                           (gesture/on-start
                                            (fn [_event]
                                              (rf/dispatch [:messages-home/select-tab :tab/recent]))))
        bottom-tabs-blur-overlay-style (style/bottom-tabs-blur-overlay (:bottom-tabs-height
                                                                        shared-values))]
    (utils/load-stack @state/selected-stack-id)
    (reanimated/set-shared-value (:pass-through? shared-values) pass-through?)
    [reanimated/view
     {:style (style/bottom-tabs-container pass-through? (:bottom-tabs-height shared-values))}
     (when pass-through?
       [reanimated/blur-view (blur-overlay-params bottom-tabs-blur-overlay-style)])
     [rn/view {:style (style/bottom-tabs)}
      [gesture/gesture-detector {:gesture communities-double-tab-gesture}
       [bottom-tab :i/communities :communities-stack shared-values notifications-data]]
      [gesture/gesture-detector {:gesture messages-double-tap-gesture}
       [bottom-tab :i/messages :chats-stack shared-values notifications-data]]
      [bottom-tab :i/wallet :wallet-stack shared-values notifications-data]
      [bottom-tab :i/browser :browser-stack shared-values notifications-data]]]))
