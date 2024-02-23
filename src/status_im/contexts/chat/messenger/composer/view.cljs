(ns status-im.contexts.chat.messenger.composer.view
  (:require
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.hooks :as hooks]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im.contexts.chat.messenger.composer.actions.view :as actions]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.edit.view :as edit]
    [status-im.contexts.chat.messenger.composer.effects :as effects]
    [status-im.contexts.chat.messenger.composer.gesture :as drag-gesture]
    [status-im.contexts.chat.messenger.composer.gradients.view :as gradients]
    [status-im.contexts.chat.messenger.composer.handlers :as handler]
    [status-im.contexts.chat.messenger.composer.images.view :as images]
    [status-im.contexts.chat.messenger.composer.link-preview.view :as link-preview]
    [status-im.contexts.chat.messenger.composer.mentions.view :as mentions]
    [status-im.contexts.chat.messenger.composer.reply.view :as reply]
    [status-im.contexts.chat.messenger.composer.selection :as selection]
    [status-im.contexts.chat.messenger.composer.style :as style]
    [status-im.contexts.chat.messenger.composer.sub-view :as sub-view]
    [status-im.contexts.chat.messenger.composer.utils :as utils]
    [status-im.contexts.chat.messenger.messages.contact-requests.bottom-drawer.view :as
     contact-requests.bottom-drawer]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn sheet-component
  [{:keys [insets
           chat-list-scroll-y
           chat-screen-layout-calculations-complete?
           theme]}
   shared-values]
  (let [subscriptions            (utils/init-subs)
        props                    (utils/init-non-reactive-state)
        state                    (utils/init-reactive-state)
        window-height            (:height (rn/get-window))
        content-height           (reagent/atom (or (:input-content-height ; Actual text height
                                                    subscriptions)
                                                   constants/input-height))
        {:keys [keyboard-shown]} (hooks/use-keyboard)
        max-height               (utils/calc-max-height subscriptions ; Max allowed height for the
                                                                      ; composer view
                                                        window-height
                                                        @(:kb-height state)
                                                        insets)
        lines                    (utils/calc-lines (- @content-height constants/extra-content-offset)) ; Current
                                                                                                       ; lines
                                                                                                       ; count
        ;; Maximum number of lines that can be displayed when composer in maximized
        max-lines                (utils/calc-lines max-height)
        animations               (utils/init-animations
                                  subscriptions
                                  lines
                                  content-height
                                  max-height
                                  shared-values)
        dimensions               {:content-height content-height
                                  :max-height     max-height
                                  :window-height  window-height
                                  :lines          lines
                                  :max-lines      max-lines}
        show-bottom-gradient?    (utils/show-bottom-gradient? state dimensions)
        ;; Cursor position, needed to determine where to display the mentions view
        cursor-pos               (utils/cursor-y-position-relative-to-container
                                  props
                                  state)]
    (effects/did-mount props)
    (effects/initialize props
                        state
                        animations
                        dimensions
                        subscriptions)
    (effects/use-edit props state subscriptions chat-screen-layout-calculations-complete?)
    (effects/use-reply props subscriptions chat-screen-layout-calculations-complete?)
    (effects/update-input-mention props state subscriptions)
    (effects/link-previews props state animations subscriptions)
    (effects/use-images props state animations subscriptions)
    [:<>
     [mentions/view props state animations max-height cursor-pos
      (:images subscriptions)
      (:link-previews? subscriptions)
      (:reply subscriptions)
      (:edit subscriptions)]
     [rn/view
      {:style style/absolute-bottom-style}
      [sub-view/shell-button shared-values chat-list-scroll-y window-height]
      [gesture/gesture-detector
       {:gesture
        (drag-gesture/drag-gesture props state animations dimensions keyboard-shown)}
       [reanimated/view
        {:style (style/sheet-container insets animations theme)}
        [sub-view/bar]
        [:<>
         [reply/view state (:input-ref props)]
         [edit/view
          {:text-value (:text-value state)
           :input-ref  (:input-ref props)}]]
        [reanimated/touchable-opacity
         {:active-opacity      1
          :on-press            (fn []
                                 (when-let [ref @(:input-ref props)]
                                   (.focus ^js ref)))
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
            :on-blur                  #(handler/blur state animations dimensions subscriptions)
            :on-content-size-change   #(handler/content-size-change %
                                                                    state
                                                                    animations
                                                                    dimensions
                                                                    (or keyboard-shown
                                                                        (:edit subscriptions)))
            :on-scroll                #(handler/scroll % props state animations dimensions)
            :on-change-text           #(handler/change-text % props state)
            :on-selection-change      #(handler/selection-change % props state)
            :on-selection             #(selection/on-selection % props state)
            :keyboard-appearance      (quo.theme/theme-value :light :dark)
            :max-font-size-multiplier 1
            :multiline                true
            :placeholder              (i18n/label :t/type-something)
            :placeholder-text-color   (colors/theme-colors colors/neutral-40 colors/neutral-50)
            :style                    (style/input-text props
                                                        state
                                                        {:max-height max-height
                                                         :theme      theme})
            :max-length               constants/max-text-size
            :accessibility-label      :chat-message-input}]]]
        [:<>
         [gradients/view props state animations show-bottom-gradient?]
         [link-preview/view]
         [images/images-list]]
        [:f> actions/view props state animations window-height subscriptions]]]]]))

(defn f-composer
  [props]
  (let [theme                   (quo.theme/use-theme-value)
        composer-default-height (+ constants/composer-default-height (:bottom (:insets props)))
        shared-values           (utils/shared-values)]
    [:<>
     ;; [reanimated/view {:style (style/background-overlay shared-values)}]
     [:f> sheet-component props shared-values theme]
     [reanimated/view {:style (style/blur-container composer-default-height shared-values)}
      [blur/view (style/blur-view theme)]]]))

(defn composer
  [props]
  (let [{:keys [chat-id
                contact-request-state
                group-chat
                able-to-send-message?]
         :as   chat} (rf/sub [:chats/current-chat-chat-view])]
    (when (seq chat)
      (if able-to-send-message?
        [:f> f-composer props]
        [contact-requests.bottom-drawer/view chat-id contact-request-state group-chat]))))
