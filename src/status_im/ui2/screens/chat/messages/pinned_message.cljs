(ns status-im.ui2.screens.chat.messages.pinned-message
  (:require [status-im.i18n.i18n :as i18n]
            [quo.react :as react]
            [react-native.reanimated :as reanimated]
            [quo.react-native :as rn]
            [quo2.foundations.typography :as typography]
            [quo2.foundations.colors :as colors]
            [status-im.switcher.constants :as constants]
            [status-im.chat.models.pin-message :as models.pin-message]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.ui2.screens.chat.messages.style :as style]
            [quo2.components.icon :as icons]))

(defn pin-limit-popover [chat-id pinned-messages-list]
  [:f>
   (fn []
     (let [{:keys [width]} (constants/dimensions)
           show-pin-limit-modal? (<sub [:chats/pin-modal chat-id])
           opacity-animation     (reanimated/use-shared-value 0)
           z-index-animation     (reanimated/use-shared-value -1)]
       (react/effect! #(do
                         (reanimated/set-shared-value opacity-animation (reanimated/with-timing (if show-pin-limit-modal? 1 0)))
                         (reanimated/set-shared-value z-index-animation (reanimated/with-timing (if show-pin-limit-modal? 10 -1)))))
       [reanimated/view {:style               (reanimated/apply-animations-to-style
                                               {:opacity opacity-animation
                                                :z-index z-index-animation}
                                               (style/pin-popover width))
                         :accessibility-label :pin-limit-popover}
        [rn/view {:style (style/pin-alert-container)}
         [rn/view {:style (style/pin-alert-circle)}
          [rn/text {:style {:color colors/danger-50}} "!"]]]
        [rn/view {:style {:margin-left 8}}
         [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold {:color (colors/theme-colors colors/white colors/neutral-100)})} (i18n/label :t/cannot-pin-title)]
         [rn/text {:style (merge typography/paragraph-2 typography/font-regular {:color (colors/theme-colors colors/white colors/neutral-100)})} (i18n/label :t/cannot-pin-desc)]
         [rn/touchable-opacity
          {:accessibility-label :view-pinned-messages
           :active-opacity      1
           :on-press            (fn []
                                  (>evt [::models.pin-message/hide-pin-limit-modal chat-id])
                                  (>evt [:bottom-sheet/show-sheet
                                         {:content #(pinned-messages-list chat-id)}]))
           :style               (style/view-pinned-messages)}
          [rn/text {:style (merge typography/paragraph-2 typography/font-medium {:color colors/white})} (i18n/label :t/view-pinned-messages)]]]
        [rn/touchable-opacity {:accessibility-label :close-pin-limit-popover
                               :active-opacity      1
                               :on-press            #(>evt [::models.pin-message/hide-pin-limit-modal chat-id])
                               :style               {:position :absolute
                                                     :top      16
                                                     :right    16}}
         [icons/icon :i/close {:color  (colors/theme-colors colors/white colors/neutral-100)
                               :height 8
                               :width  8}]]]))])
