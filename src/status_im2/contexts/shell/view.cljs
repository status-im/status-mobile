(ns status-im2.contexts.shell.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [oops.core :refer [oget]]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
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
  [{:keys [id type channel-id] :as card}]
  (let [card-data (case type
                    shell.constants/one-to-one-chat-card
                    (rf/sub [:shell/one-to-one-chat-card id])

                    shell.constants/private-group-chat-card
                    (rf/sub [:shell/private-group-chat-card id])

                    shell.constants/community-card
                    (rf/sub [:shell/community-card id])

                    shell.constants/community-channel-card
                    (rf/sub [:shell/community-channel-card channel-id])

                    nil)]
    [switcher-cards/card (merge card card-data)]))

(def empty-cards (repeat 6 {:type shell.constants/empty-card}))

(defn jump-to-list
  [switcher-cards shell-margin]
  (let [data (if (seq switcher-cards) switcher-cards empty-cards)]
    [:<>
     [rn/flat-list
      {:data                 data
       :render-fn            render-card
       :key-fn               :id
       :header               (jump-to-text)
       :num-columns          2
       :column-wrapper-style {:margin-horizontal shell-margin
                              :justify-content   :space-between
                              :margin-bottom     16}
       :style                {:top      0
                              :left     0
                              :right    0
                              :bottom   -1
                              :position :absolute}}]
     (when-not (seq switcher-cards)
       [placeholder])]))

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
        [common.home/top-nav
         {:type  :shell
          :style {:margin-top (:top insets)
                  :z-index    2}}]
        [jump-to-list switcher-cards shell-margin]])]))

(defn shell-stack
  []
  [:f>
   (fn []
     (let [shared-values (animation/calculate-shared-values)]
       [rn/view
        {:style     {:flex 1}
         :on-layout (when-not @animation/screen-height
                      (fn [evt]
                        (let [height (oget evt "nativeEvent" "layout" "height")]
                          (reset! animation/screen-height height)
                          (async-storage/set-item! :screen-height height))))}
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
