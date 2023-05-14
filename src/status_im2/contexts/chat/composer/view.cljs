(ns status-im2.contexts.chat.composer.view
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [status-im2.contexts.chat.composer.style :as style]
    [status-im2.contexts.chat.composer.images.view :as images]
    [status-im2.contexts.chat.composer.reply.view :as reply]
    [status-im2.contexts.chat.composer.edit.view :as edit]
    [status-im2.contexts.chat.composer.mentions.view :as mentions]
    [status-im2.contexts.chat.composer.utils :as utils]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.actions.view :as actions]
    [status-im2.contexts.chat.composer.sub-view :as sub-view]
    [status-im2.contexts.chat.composer.effects :as effects]
    [status-im2.contexts.chat.composer.gesture :as drag-gesture]
    [status-im2.contexts.chat.composer.handlers :as handler]
    [status-im2.contexts.chat.composer.gradients.view :as gradients]
    [status-im2.contexts.chat.composer.selection :as selection]))

(defn sheet-component
  [{:keys [insets window-height blur-height opacity background-y]} props state]
  (let [subs                     (utils/init-subs)
        content-height           (reagent/atom (or (:input-content-height subs)
                                                   constants/input-height))
        {:keys [keyboard-shown]} (hooks/use-keyboard)
        max-height               (utils/calc-max-height subs
                                                        window-height
                                                        @(:kb-height state)
                                                        insets)
        lines                    (utils/calc-lines (- @content-height
                                                      constants/extra-content-offset))
        max-lines                (utils/calc-lines max-height)
        animations               (utils/init-animations
                                  subs
                                  lines
                                  content-height
                                  max-height
                                  opacity
                                  background-y)
        dimensions               {:content-height content-height
                                  :max-height     max-height
                                  :window-height  window-height
                                  :lines          lines
                                  :max-lines      max-lines}
        show-bottom-gradient?    (utils/show-bottom-gradient? state dimensions)
        cursor-pos               (utils/cursor-y-position-relative-to-container props
                                                                                state)]
    (effects/did-mount props)
    (effects/initialize props
                        state
                        animations
                        dimensions
                        subs)
    (effects/edit props state subs)
    (effects/reply props animations subs)
    (effects/update-input-mention props state subs)
    (effects/edit-mentions props state subs)
    [:<>
     [sub-view/shell-button state animations subs]
     [mentions/view props state animations max-height cursor-pos (:images subs) (:reply subs)
      (:edit subs)]
     [gesture/gesture-detector
      {:gesture (drag-gesture/drag-gesture props state animations dimensions keyboard-shown)}
      [reanimated/view
       {:style     (style/sheet-container insets state animations)
        :on-layout #(handler/layout % state blur-height)}
       [sub-view/bar]
       [reply/view state]
       [edit/view state]
       [reanimated/touchable-opacity
        {:active-opacity      1
         :on-press            (when @(:input-ref props) #(.focus ^js @(:input-ref props)))
         :style               (style/input-container (:height animations) max-height)
         :accessibility-label :message-input-container}
        [rn/selectable-text-input
         {:ref        #(reset! (:selectable-input-ref props) %)
          :menu-items @(:menu-items state)
          :style      (style/input-view state)}
         [rn/text-input
          {:ref                      #(reset! (:input-ref props) %)
           :default-value            @(:text-value state)
           :on-focus                 #(handler/focus props state animations dimensions)
           :on-blur                  #(handler/blur state animations dimensions subs)
           :on-content-size-change   #(handler/content-size-change %
                                                                   state
                                                                   animations
                                                                   dimensions
                                                                   (or keyboard-shown (:edit subs)))
           :on-scroll                #(handler/scroll % props state animations dimensions)
           :on-change-text           #(handler/change-text % props state)
           :on-selection-change      #(handler/selection-change % props state)
           :on-selection             #(selection/on-selection % props state)
           :max-height               max-height
           :max-font-size-multiplier 1
           :multiline                true
           :placeholder              (i18n/label :t/type-something)
           :placeholder-text-color   (colors/theme-colors colors/neutral-40 colors/neutral-50)
           :style                    (style/input-text props state)
           :max-length               constants/max-text-size
           :accessibility-label      :chat-message-input}]]
        [gradients/view props state animations show-bottom-gradient?]]
       [images/images-list]
       [actions/view props state animations window-height insets subs]]]]))

(defn composer
  [insets]
  (let [window-height (:height (rn/get-window))
        opacity       (reanimated/use-shared-value 0)
        background-y  (reanimated/use-shared-value (- window-height))
        blur-height   (reanimated/use-shared-value (+ constants/composer-default-height
                                                      (:bottom insets)))
        extra-params  {:insets        insets
                       :window-height window-height
                       :blur-height   blur-height
                       :opacity       opacity
                       :background-y  background-y}
        props         (utils/init-props)
        state         (utils/init-state)]
    [rn/view
     [reanimated/view {:style (style/background opacity background-y window-height)}]
     [sub-view/blur-view blur-height (:focused? state)]
     [:f> sheet-component extra-params props state]]))
