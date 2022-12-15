(ns status-im2.contexts.shell.view
  (:require [quo2.core :as quo]
            [i18n.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]
            [status-im2.common.home.view :as common.home]
            [status-im2.contexts.shell.constants :as constants]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.bottom-tabs :as bottom-tabs]
            [status-im2.contexts.shell.cards.view :as switcher-cards]
            [status-im2.contexts.shell.stacks.chat-stack :as chat-stack]
            [status-im2.contexts.shell.stacks.home-stack :as home-stack]))

;; TODO
;; 1 : Update Placeholder screen as per new designs
;; 2 : Move styles to style namespace
(defn placeholder []
  [rn/view {:style {:position            :absolute
                    :top                 0
                    :left                0
                    :right               0
                    :bottom              -1
                    :justify-content     :center
                    :align-items         :center
                    :accessibility-label :shell-placeholder-view}}
   [rn/view {:style {:margin-top       12
                     :width            80
                     :height           80
                     :border-radius    16
                     :background-color colors/neutral-90}}]
   [quo/text {:size   :heading-2
              :weight :semi-bold
              :style  {:margin-top 20
                       :color      colors/white}}
    (i18n/label :t/shell-placeholder-title)]
   [quo/text {:size   :paragraph-1
              :weight :regular
              :align  :center
              :style  {:margin-top 8
                       :color      colors/white}}
    (i18n/label :t/shell-placeholder-subtitle)]])

(defn jump-to-text []
  [quo/text {:size   :heading-1
             :weight :semi-bold
             :style  {:color         colors/white
                      :margin-top    (+ 68 (.-currentHeight ^js rn/status-bar))
                      :margin-bottom 20
                      :margin-left   20}}
   (i18n/label :t/jump-to)])

(defn render-card [{:keys [id type content] :as card}]
  (let [card-data (case type
                    constants/one-to-one-chat-card
                    (rf/sub [:shell/one-to-one-chat-card id])

                    constants/private-group-chat-card
                    (rf/sub [:shell/private-group-chat-card id])

                    constants/community-card
                    (if content
                      (rf/sub [:shell/community-channel-card
                               id (get-in content [:data :channel-id])
                               content])
                      (rf/sub [:shell/community-card id])))]
    [switcher-cards/card (merge card card-data)]))

(defn jump-to-list [switcher-cards shell-margin]
  (if (seq switcher-cards)
    [rn/flat-list
     {:data                 switcher-cards
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
    [placeholder]))

(defn shell []
  (let [switcher-cards (rf/sub [:shell/sorted-switcher-cards])
        width          (rf/sub [:dimensions/window-width])
        shell-margin   (/ (- width 320) 3)] ;; 320 - two cards width
    [safe-area/consumer
     (fn [insets]
       [rn/view {:style {:top              0
                         :left             0
                         :right            0
                         :bottom           -1
                         :position         :absolute
                         :background-color colors/neutral-100}}
        [common.home/top-nav {:type  :shell
                              :style {:margin-top (:top insets)
                                      :z-index    2}}]
        [jump-to-list switcher-cards shell-margin]])]))

(defn shell-stack []
  [:f>
   (fn []
     (let [shared-values (animation/calculate-shared-values)]
       [:<>
        [shell]
        [bottom-tabs/bottom-tabs]
        [home-stack/home-stack]
        [chat-stack/chat-stack]
        [quo/floating-shell-button
         {:jump-to {:on-press #(animation/close-home-stack true)
                    :label (i18n/label :t/jump-to)}}
         {:position :absolute
          :bottom   (+ (constants/bottom-tabs-container-height) 7)} ;; bottom offset is 12 = 7 + 5(padding on button)
         (:home-stack-opacity shared-values)]]))])
