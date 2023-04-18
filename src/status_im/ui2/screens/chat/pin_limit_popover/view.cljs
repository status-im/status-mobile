(ns status-im.ui2.screens.chat.pin-limit-popover.view
  (:require [utils.i18n :as i18n]
            [quo.react :as react]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.ui2.screens.chat.pin-limit-popover.style :as style]
            [utils.re-frame :as rf]))

;; TODO (flexsurfer) this should be an in-app notification component in quo2
;; https://github.com/status-im/status-mobile/issues/14527
(defn pin-limit-popover
  [chat-id]
  [:f>
   (fn []
     (let [width                 (rf/sub [:dimensions/window-width])
           show-pin-limit-modal? (rf/sub [:chats/pin-modal chat-id])
           opacity-animation     (reanimated/use-shared-value 0)
           z-index-animation     (reanimated/use-shared-value -1)]
       (react/effect!
        #(do
           (reanimated/set-shared-value opacity-animation
                                        (reanimated/with-timing (if show-pin-limit-modal? 1 0)))
           (reanimated/set-shared-value z-index-animation
                                        (reanimated/with-timing (if show-pin-limit-modal? 10 -1)))))
       (when show-pin-limit-modal?
         [reanimated/view
          {:style               (reanimated/apply-animations-to-style
                                 {:opacity opacity-animation
                                  :z-index z-index-animation}
                                 (style/pin-popover width))
           :accessibility-label :pin-limit-popover}
          [rn/view {:style (style/pin-alert-container)}
           [rn/view {:style style/pin-alert-circle}
            [rn/text {:style {:color colors/danger-50}} "!"]]]
          [rn/view {:style {:margin-left 8}}
           [quo/text {:weight :semi-bold :color (colors/theme-colors colors/white colors/neutral-100)}
            (i18n/label :t/cannot-pin-title)]
           [quo/text {:size :paragraph-2 :color (colors/theme-colors colors/white colors/neutral-100)}
            (i18n/label :t/cannot-pin-desc)]
           [rn/touchable-opacity
            {:accessibility-label :view-pinned-messages
             :active-opacity      1
             :on-press            (fn []
                                    (rf/dispatch [:pin-message/hide-pin-limit-modal chat-id])
                                    (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id])
                                    (rf/dispatch [:dismiss-keyboard]))
             :style               style/view-pinned-messages}
            [quo/text {:size :paragraph-2 :weight :medium :color colors/white}
             (i18n/label :t/view-pinned-messages)]]]
          [rn/touchable-opacity
           {:accessibility-label :close-pin-limit-popover
            :active-opacity      1
            :on-press            #(rf/dispatch [:pin-message/hide-pin-limit-modal chat-id])
            :style               {:position :absolute
                                  :top      16
                                  :right    16}}
           [quo/icon :i/close
            {:color (colors/theme-colors colors/white colors/neutral-100)
             :size  12}]]])))])
