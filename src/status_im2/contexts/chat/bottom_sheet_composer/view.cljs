(ns status-im2.contexts.chat.bottom-sheet-composer.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.bottom-sheet-composer.style :as style]
    [status-im2.contexts.chat.bottom-sheet-composer.images.view :as images]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.bottom-sheet-composer.utils :as utils]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as constants]
    [status-im2.contexts.chat.bottom-sheet-composer.actions.view :as actions]
    [status-im2.contexts.chat.bottom-sheet-composer.keyboard :as kb]
    [status-im2.contexts.chat.bottom-sheet-composer.sub-view :as sub-view]
    [status-im2.contexts.chat.bottom-sheet-composer.effects :as effects]
    [status-im2.contexts.chat.bottom-sheet-composer.gesture :as drag-gesture]
    [status-im2.contexts.chat.bottom-sheet-composer.handlers :as handler]
    [status-im2.contexts.chat.bottom-sheet-composer.gradients.view :as gradients]))

(defn sheet
  [insets window-height blur-height opacity background-y]
  [:f>
   (fn []
     (let [props {:input-ref                   (atom nil)
                  :keyboard-show-listener      (atom nil)
                  :keyboard-frame-listener     (atom nil)
                  :keyboard-hide-listener      (atom nil)
                  :emoji-kb-extra-height       (atom nil)
                  :saved-emoji-kb-extra-height (atom nil)}
           state {:text-value            (reagent/atom "")
                  :cursor-position       (reagent/atom 0)
                  :saved-cursor-position (reagent/atom 0)
                  :gradient-z-index      (reagent/atom 0)
                  :kb-default-height     (reagent/atom nil)
                  :gesture-enabled?      (reagent/atom true)
                  :lock-selection?       (reagent/atom true)
                  :focused?              (reagent/atom false)
                  :lock-layout?          (reagent/atom false)
                  :maximized?            (reagent/atom false)}]
       [:f>
        (fn []
          (let [images                                   (rf/sub [:chats/sending-image])
                {:keys [input-text input-content-height]
                 :as   chat-input}                       (rf/sub [:chats/current-chat-input])
                content-height                           (reagent/atom (or input-content-height
                                                                           constants/input-height))
                {:keys [keyboard-shown keyboard-height]} (hooks/use-keyboard)
                kb-height                                (kb/get-kb-height keyboard-height
                                                                           @(:kb-default-height state))
                max-height                               (utils/calc-max-height window-height
                                                                                kb-height
                                                                                insets
                                                                                images)
                lines                                    (utils/calc-lines @content-height)
                max-lines                                (utils/calc-lines max-height)
                initial-height                           (if (> lines 1)
                                                           constants/multiline-minimized-height
                                                           constants/input-height)
                animations                               {:gradient-opacity  (reanimated/use-shared-value
                                                                              0)
                                                          :container-opacity (reanimated/use-shared-value
                                                                              (if (utils/empty-input?
                                                                                   input-text
                                                                                   images)
                                                                                0.7
                                                                                1))
                                                          :height            (reanimated/use-shared-value
                                                                              initial-height)
                                                          :saved-height      (reanimated/use-shared-value
                                                                              initial-height)
                                                          :last-height       (reanimated/use-shared-value
                                                                              (utils/bounded-val
                                                                               @content-height
                                                                               constants/input-height
                                                                               max-height))
                                                          :opacity           opacity
                                                          :background-y      background-y}
                dimensions                               {:content-height content-height
                                                          :max-height     max-height
                                                          :window-height  window-height
                                                          :lines          lines
                                                          :max-lines      max-lines}
                show-bottom-gradient? (utils/show-bottom-gradient? state dimensions)]
            (effects/initialize props
                                state
                                animations
                                dimensions
                                chat-input
                                keyboard-height
                                (seq images))
            [gesture/gesture-detector
             {:gesture (drag-gesture/drag-gesture props state animations dimensions keyboard-shown)}
             [reanimated/view
              {:style     (style/sheet-container insets (:container-opacity animations) lines)
               :on-layout #(handler/layout % state blur-height)}
              [sub-view/bar]
              [reanimated/touchable-opacity
               {:active-opacity      1
                :on-press            (when @(:input-ref props) #(.focus ^js @(:input-ref props)))
                :style               (style/input-container (:height animations) max-height)
                :accessibility-label :message-input-container}
               [rn/text-input
                {:ref                      #(reset! (:input-ref props) %)
                 :default-value            @(:text-value state)
                 :on-focus                 #(handler/focus props state animations dimensions)
                 :on-blur                  #(handler/blur state animations dimensions images)
                 :on-content-size-change   #(handler/content-size-change %
                                                                         state
                                                                         animations
                                                                         dimensions
                                                                         keyboard-shown)
                 :on-scroll                #(handler/scroll % state animations dimensions)
                 :on-change-text           #(handler/change-text % props state)
                 :on-selection-change      #(handler/selection-change % state)
                 :max-height               max-height
                 :max-font-size-multiplier 1
                 :multiline                true
                 :placeholder              (i18n/label :t/type-something)
                 :placeholder-text-color   (colors/theme-colors colors/neutral-40 colors/neutral-50)
                 :style                    (style/input @(:maximized? state)
                                                        @(:saved-emoji-kb-extra-height props))
                 :accessibility-label      :chat-message-input}]
               [gradients/view props state animations show-bottom-gradient?]]
              [images/images-list]
              [actions/view props state animations window-height insets (seq images)]]]))]))])

(defn bottom-sheet-composer
  [insets]
  [:f>
   (fn []
     (let [window-height (rf/sub [:dimensions/window-height])
           opacity       (reanimated/use-shared-value 0)
           background-y  (reanimated/use-shared-value (- window-height))
           blur-height   (reanimated/use-shared-value (+ constants/composer-default-height
                                                         (:bottom insets)))]
       [rn/view
        [reanimated/view {:style (style/background opacity background-y window-height)}]
        [sub-view/blur-view blur-height]
        [sheet insets window-height blur-height opacity background-y]]))])
