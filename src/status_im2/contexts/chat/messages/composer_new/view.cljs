(ns status-im2.contexts.chat.messages.composer-new.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.typography :as typography]
    [react-native.background-timer :as background-timer]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [oops.core :as oops]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.utils.utils :as utils-old]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.messages.composer-new.style :as style]
    [utils.re-frame :as rf]))

(def ^:const drag-threshold 200)

(def input-height 32)

;;; CONTROLS
(defn image-button
  [chat-id]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                  {:permissions [:read-external-storage :write-external-storage]
                   :on-allowed  #(rf/dispatch
                                   [:open-modal :photo-selector {:chat-id chat-id}])
                   :on-denied   (fn []
                                  (background-timer/set-timeout
                                    #(utils-old/show-popup (i18n/label :t/error)
                                                           (i18n/label
                                                             :t/external-storage-denied))
                                    50))}))
    :icon     true
    :type     :outline
    :size     32}
   :i/image])

(defn drag-gesture
  [value saved-value keyboard-shown max-height]
  (->
    (gesture/gesture-pan)
    (gesture/on-update (fn [e]
                         (let [translation (oops/oget e "translationY")]
                           (when keyboard-shown
                             (reanimated/set-shared-value value (Math/min (+ (- translation) (reanimated/get-shared-value saved-value)) max-height))))))
    (gesture/on-end (fn [e]
                      (reanimated/set-shared-value saved-value (reanimated/get-shared-value value))))
    ))

;;; MAIN
(defn composer
  []
  [:f> (fn [] (let [value       (reanimated/use-shared-value input-height)
                    saved-value (reanimated/use-shared-value input-height)
                    text-value  (reagent/atom "")]
                [:f>
                 (fn []
                   (let [{:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
                         window-height (rf/sub [:dimensions/window-height])
                         insets        (safe-area/use-safe-area)

                         line-height   (:line-height typography/paragraph-1)
                         margin-top    (if platform/ios? (:top insets) (+ (:top insets) 10))
                         max-height    (- window-height margin-top keyboard-height style/handle-container-height style/actions-container-height)]
                     [gesture/gesture-detector {:gesture (drag-gesture value saved-value keyboard-shown max-height)}
                      [rn/keyboard-avoiding-view {:style                    (style/container insets)
                                                  :behavior                 (if platform/ios? "padding" nil)
                                                  :keyboard-vertical-offset 12}
                       [rn/view {:style {:padding-bottom (:bottom insets)}}
                        [rn/view {:style (style/handle-container)}
                         [rn/view {:style (style/handle)}]]
                        [reanimated/text-input
                         {:style                  (style/input value)
                          ;:on-change-text         #(reset! text-value %)
                          ;:value                  @text-value
                          ;:on-content-size-change (fn [e]
                          ;                          (let [x (oops/oget e "nativeEvent.contentSize.height")]
                          ;                            (when (<= x max-height)
                          ;                              (reanimated/set-shared-value value x)
                          ;                              (reanimated/set-shared-value saved-value x))
                          ;                            ))
                          :text-align-vertical    :top
                          :max-height             max-height
                          :multiline              true
                          :placeholder-text-color (colors/theme-colors colors/neutral-30 colors/neutral-60)
                          :placeholder            (i18n/label :t/type-something)}]
                        [rn/view {:style (style/actions-container)}
                         [image-button]]]]]))]))])

