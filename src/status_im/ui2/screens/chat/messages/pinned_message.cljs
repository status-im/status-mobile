(ns status-im.ui2.screens.chat.messages.pinned-message
  (:require [status-im.i18n.i18n :as i18n]
            [quo.react :as react]
            [quo2.reanimated :as reanimated]
            [quo.react-native :as rn]
            [quo2.foundations.typography :as typography]
            [quo2.foundations.colors :as quo2.colors]
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
           opacity-animation (reanimated/use-shared-value 0)
           z-index-animation (reanimated/use-shared-value -1)]
       (println "show" show-pin-limit-modal? chat-id)
       (react/effect! #(do
                         (reanimated/set-shared-value opacity-animation (reanimated/with-timing (if show-pin-limit-modal? 1 0)))
                         (reanimated/set-shared-value z-index-animation (reanimated/with-timing (if show-pin-limit-modal? 10 -1)))))
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                  {:opacity opacity-animation
                                   :z-index z-index-animation}
                                  (style/pin-popover width))}
        [rn/view {:style (style/pin-alert-container)}
         [rn/view {:style (style/pin-alert-circle)}
          [rn/text {:style {:color quo2.colors/danger-50}} "!"]]]
        [rn/view {:style {:margin-left 8}}
         [rn/text {:style (merge typography/paragraph-1 typography/font-semi-bold {:color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-100)})} (i18n/label :t/cannot-pin-title)]
         [rn/text {:style (merge typography/paragraph-2 typography/font-regular {:color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-100)})} (i18n/label :t/cannot-pin-desc)]
         [rn/touchable-opacity
          {:active-opacity 1
           :on-press (fn []
                       (>evt [::models.pin-message/hide-pin-limit-modal chat-id])
                       (>evt [:bottom-sheet/show-sheet
                                           {:content #(pinned-messages-list chat-id)}]))
           :style (style/view-pinned-messages)}
          [rn/text {:style (merge typography/paragraph-2 typography/font-medium {:color quo2.colors/white})} (i18n/label :t/view-pinned-messages)]]]
        [rn/touchable-opacity {:active-opacity 1
                               :on-press       #(>evt [::models.pin-message/hide-pin-limit-modal chat-id])
                               :style          {:position :absolute
                                       :top 16
                                       :right 16}}
         [icons/icon :main-icons2/close {:color  (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-100)
                                        :height 8
                                        :width  8}]]]))])
