(ns status-im.ui.screens.chat.components.input
  (:require [status-im.ui.components.icons.vector-icons :as icons]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.platform :as platform]
            [quo.design-system.colors :as colors]
            [status-im.ui.screens.chat.components.style :as styles]
            [status-im.ui.screens.chat.components.reply :as reply]
            [status-im.utils.utils :as utils.utils]
            [quo.components.animated.pressable :as pressable]
            [quo.animated :as animated]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

(def panel->icons {:extensions :main-icons/commands
                   :images     :main-icons/photo
                   :audio      :main-icons/speech})

(defn touchable-icon [{:keys [panel active set-active accessibility-label]}]
  [pressable/pressable {:type                :scale
                        :accessibility-label accessibility-label
                        :on-press            #(set-active (when-not (= active panel) panel))}
   [rn/view {:style (styles/touchable-icon)}
    [icons/icon
     (panel->icons panel)
     (styles/icon (= active panel))]]])

(defn touchable-stickers-icon [{:keys [panel active set-active accessibility-label input-focus]}]
  [pressable/pressable {:type                :scale
                        :accessibility-label accessibility-label
                        :on-press            #(if (= active panel)
                                                (input-focus)
                                                (set-active panel))}
   [rn/view {:style (styles/in-input-touchable-icon)}
    (if (= active panel)
      [icons/icon :main-icons/keyboard (styles/icon false)]
      [icons/icon :main-icons/stickers (styles/icon false)])]])

(defn- request-record-audio-permission [set-active panel]
  (re-frame/dispatch
   [:request-permissions
    {:permissions [:record-audio]
     :on-allowed
     #(set-active panel)
     :on-denied
     #(utils.utils/set-timeout
       (fn []
         (utils.utils/show-popup (i18n/label :t/audio-recorder-error)
                                 (i18n/label :t/audio-recorder-permissions-error)))
       50)}]))

(defn touchable-audio-icon [{:keys [panel active set-active accessibility-label input-focus]}]
  [pressable/pressable {:type                :scale
                        :accessibility-label accessibility-label
                        :on-press            #(if (= active panel)
                                                (input-focus)
                                                (request-record-audio-permission set-active panel))}
   [rn/view {:style (styles/in-input-touchable-icon)}
    [icons/icon
     (panel->icons panel)
     (styles/icon (= active panel))]]])

(defn send-button [{:keys [on-send-press]}]
  [pressable/pressable {:type     :scale
                        :on-press on-send-press}
   [rn/view {:style (styles/send-message-button)}
    [icons/icon :main-icons/arrow-up
     {:container-style     (styles/send-message-container)
      :accessibility-label :send-message-button
      :color               (styles/send-icon-color)}]]])

(defn text-input [{:keys [cooldown-enabled? text-value on-text-change set-active-panel text-input-ref]}]
  [rn/view {:style (styles/text-input-wrapper)}
   [rn/text-input {:style                  (styles/text-input)
                   :ref                    text-input-ref
                   :maxFontSizeMultiplier  1
                   :accessibility-label    :chat-message-input
                   :text-align-vertical    :center
                   :multiline              true
                   :default-value          text-value
                   :editable               (not cooldown-enabled?)
                   :blur-on-submit         false
                   :auto-focus             false
                   :on-focus               #(set-active-panel nil)
                   :on-change              #(on-text-change (.-text ^js (.-nativeEvent ^js %)))
                   :placeholder-text-color (:text-02 @colors/theme)
                   :placeholder            (if cooldown-enabled?
                                             (i18n/label :cooldown/text-input-disabled)
                                             (i18n/label :t/type-a-message))
                   :underlineColorAndroid  :transparent
                   :auto-capitalize        :sentences}]])

(defn chat-input
  [{:keys [set-active-panel active-panel on-send-press reply
           show-send show-image show-stickers show-extensions
           sending-image input-focus show-audio]
    :as   props}]
  [rn/view {:style (styles/toolbar)}
   [rn/view {:style (styles/actions-wrapper (and (not show-extensions)
                                                 (not show-image)))}
    (when show-extensions
      [touchable-icon {:panel               :extensions
                       :accessibility-label :show-extensions-icon
                       :active              active-panel
                       :set-active          set-active-panel}])
    (when show-image
      [touchable-icon {:panel               :images
                       :accessibility-label :show-photo-icon
                       :active              active-panel
                       :set-active          set-active-panel}])]
   [animated/view {:style (styles/input-container)}
    (when reply
      [reply/reply-message reply])
    (when sending-image
      [reply/send-image sending-image])
    [rn/view {:style (styles/input-row)}
     [text-input props]
     [rn/view {:style (styles/in-input-buttons)}
      (when show-send
        [send-button {:on-send-press on-send-press}])
      (when show-stickers
        [touchable-stickers-icon {:panel               :stickers
                                  :accessibility-label :show-stickers-icon
                                  :active              active-panel
                                  :input-focus         input-focus
                                  :set-active          set-active-panel}])
      (when show-audio
        [touchable-audio-icon {:panel               :audio
                               :accessibility-label :show-audio-message-icon
                               :active              active-panel
                               :input-focus         input-focus
                               :set-active          set-active-panel}])]]]])

(defn chat-toolbar []
  (let [text-input-ref  (react/create-ref)
        input-focus     (fn []
                          (some-> ^js (react/current-ref text-input-ref) .focus))
        clear-input     (fn []
                          (some-> ^js (react/current-ref text-input-ref) .clear))
        previous-layout (atom nil)
        had-reply       (atom nil)]
    (fn [{:keys [active-panel set-active-panel]}]
      (let [disconnected?        @(re-frame/subscribe [:disconnected?])
            {:keys [processing]} @(re-frame/subscribe [:multiaccounts/login])
            mainnet?             @(re-frame/subscribe [:mainnet?])
            input-text           @(re-frame/subscribe [:chats/current-chat-input-text])
            cooldown-enabled?    @(re-frame/subscribe [:chats/cooldown-enabled?])
            one-to-one-chat?     @(re-frame/subscribe [:current-chat/one-to-one-chat?])
            public?              @(re-frame/subscribe [:current-chat/public?])
            reply                @(re-frame/subscribe [:chats/reply-message])
            sending-image        @(re-frame/subscribe [:chats/sending-image])
            empty-text           (string/blank? (string/trim (or input-text "")))
            show-send            (and (or (not empty-text)
                                          sending-image)
                                      (not (or processing disconnected?)))
            show-stickers        (and empty-text
                                      mainnet?
                                      (not sending-image)
                                      (not reply))
            show-image           (and empty-text
                                      (not reply)
                                      (not public?))
            show-extensions      (and empty-text
                                      one-to-one-chat?
                                      (or config/commands-enabled? mainnet?)
                                      (not reply))
            show-audio           (and empty-text
                                      (not sending-image)
                                      (not reply)
                                      (not public?))]
        (when-not (= reply @had-reply)
          (reset! had-reply reply)
          (when reply
            (js/setTimeout input-focus 250)))
        (when (and platform/ios? (not= @previous-layout [show-send show-stickers show-extensions show-audio]))
          (reset! previous-layout [show-send show-stickers show-extensions show-audio])
          (when (seq @previous-layout)
            (rn/configure-next
             (:ease-opacity-200 rn/custom-animations))))
        [chat-input {:set-active-panel  set-active-panel
                     :active-panel      active-panel
                     :text-input-ref    text-input-ref
                     :input-focus       input-focus
                     :reply             reply
                     :on-send-press     #(do (re-frame/dispatch [:chat.ui/send-current-message])
                                             (clear-input))
                     :text-value        input-text
                     :on-text-change    #(re-frame/dispatch [:chat.ui/set-chat-input-text %])
                     :cooldown-enabled? cooldown-enabled?
                     :show-send         show-send
                     :show-stickers     show-stickers
                     :show-image        show-image
                     :show-audio        show-audio
                     :sending-image     sending-image
                     :show-extensions   show-extensions}]))))
