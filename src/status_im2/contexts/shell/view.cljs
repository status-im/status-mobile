(ns status-im2.contexts.shell.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [oops.core :refer [oget]]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.safe-area :as safe-area]
            [status-im2.common.home.view :as common.home]
            [status-im.async-storage.core :as async-storage]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.bottom-tabs :as bottom-tabs]
            [status-im2.contexts.shell.cards.view :as switcher-cards]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im2.contexts.shell.home-stack :as home-stack]
            [status-im2.contexts.shell.style :as style]
            [utils.re-frame :as rf]))

(defn placeholder
  []
  [linear-gradient/linear-gradient
   {:colors [colors/neutral-100-opa-0 colors/neutral-100-opa-100]
    :start  {:x 0 :y 0}
    :end    {:x 0 :y 1}
    :style  (style/placeholder-container (rn/status-bar-height))}
   [rn/image
    {:source nil ;; TODO(parvesh) - add placeholder image
     :style  style/placeholder-image}]
   [quo/text
    {:size   :paragraph-1
     :weight :semi-bold
     :style  style/placeholder-title}
    (i18n/label :t/shell-placeholder-title)]
   [quo/text
    {:size   :paragraph-2
     :weight :regular
     :align  :center
     :style  style/placeholder-subtitle}
    (i18n/label :t/shell-placeholder-subtitle)]])

(defn jump-to-text
  []
  [quo/text
   {:size   :heading-1
    :weight :semi-bold
    :style  (style/jump-to-text (rn/status-bar-height))}
   (i18n/label :t/jump-to)])

(defn render-card
  [{:keys [type screen-id] :as card}]
  [rn/view [rn/text {:style {:color :red}} "SOME TEXT"]]
  #_
  (let [card-data (case type
                    shell.constants/one-to-one-chat-card
                    (rf/sub [:shell/one-to-one-chat-card screen-id])

                    shell.constants/private-group-chat-card
                    (rf/sub [:shell/private-group-chat-card screen-id])

                    shell.constants/community-card
                    (rf/sub [:shell/community-card screen-id])

                    shell.constants/community-channel-card
                    (rf/sub [:shell/community-channel-card screen-id])

                    nil)]
    [switcher-cards/card (merge card card-data)]))

(def empty-cards (repeat 6 {:type shell.constants/empty-card}))

(defn jump-to-list
  [switcher-cards shell-margin]
  (let [data (if (seq switcher-cards) switcher-cards empty-cards)]
    [:<>
     [rn/flat-list
      {:data                    data
       :render-fn               render-card
       :key-fn                  :id
       :header                  (jump-to-text)
       :num-columns             2
       :column-wrapper-style    {:margin-horizontal shell-margin
                                 :justify-content   :space-between
                                 :margin-bottom     16}
       :style                   style/jump-to-list
       :content-container-style {:padding-bottom (shell.constants/bottom-tabs-container-height)}}]
     (when-not (seq switcher-cards)
       [placeholder])]))

(defn top-nav-blur-overlay
  [top]
  (let [pass-through? (rf/sub [:shell/shell-pass-through?])]
    [rn/view {:style (style/top-nav-blur-overlay-container (+ 56 top) pass-through?)}
     (when pass-through?
       [blur/view (bottom-tabs/blur-overlay-params style/top-nav-blur-overlay)])]))

(defn shell
  []
  (let [switcher-cards (rf/sub [:shell/sorted-switcher-cards])
        width          (rf/sub [:dimensions/window-width])
        shell-margin   (/ (- width 320) 3)] ;; 320 - two cards width
    [safe-area/consumer
     (fn [insets]
       [rn/view
        {:style {:top              0
                 :left             0
                 :right            0
                 :bottom           -1
                 :position         :absolute
                 :background-color colors/neutral-100}}
        [jump-to-list switcher-cards shell-margin]
        [top-nav-blur-overlay (:top insets)]
        [common.home/top-nav
         {:type  :shell
          :style {:margin-top (:top insets)
                  :z-index    2}}]])]))

(defn on-layout
  [evt]
  (let [dimensions (rf/sub [:dimensions/window])
        height     (or (oget evt "nativeEvent" "layout" "height") 0)
        width      (or (oget evt "nativeEvent" "layout" "width") 0)]
    ;; Layout height calculation
    ;; 1. Make sure height is more than width, and on-layout is not fired while the
    ;; screen is horizontal
    ;; 2. Initialize values with 0 in case of nil
    ;; 3. In the case of notch devices, the dimensions height will be smaller than
    ;; on-layout,
    ;; (without status bar height included)
    ;; https://github.com/status-im/status-mobile/issues/14633
    ;; 4. In the case of devices without a notch, both heights should be the same,
    ;; but actual values differ in some pixels, so arbitrary 5 pixels is allowed
    (when (and (> height width)
               (>= (+ height 5) (or (:height dimensions) 0)))
      (reset! animation/screen-height height)
      (async-storage/set-item! :screen-height height))))

(defn shell-stack
  []
  [:f>
   (fn []
     (let [shared-values (animation/calculate-shared-values)]
       [rn/view
        {:style     {:flex 1}
         :on-layout on-layout}
        [shell]
        [bottom-tabs/bottom-tabs]
        [home-stack/home-stack]
        [quo/floating-shell-button
         {:jump-to {:on-press #(animation/close-home-stack true)
                    :label    (i18n/label :t/jump-to)}}
         {:position :absolute
          :bottom   (+ (shell.constants/bottom-tabs-container-height) 7)} ;; bottom offset is 12 = 7 +
                                                                          ;; 5(padding on button)
         (:home-stack-opacity shared-values)]]))])
