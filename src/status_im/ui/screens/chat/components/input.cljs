(ns status-im.ui.screens.chat.components.input
  (:require [status-im.ui.components.icons.vector-icons :as icons]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.platform :as platform]
            [quo.design-system.colors :as colors]
            [status-im.ui.screens.chat.components.style :as styles]
            [status-im.ui.screens.chat.components.reply :as reply]
            [status-im.chat.constants :as chat.constants]
            [status-im.utils.utils :as utils.utils]
            [quo.components.animated.pressable :as pressable]
            [quo.animated :as animated]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [clojure.string :as string]
            [status-im.chat.models.mentions :as mentions]
            [status-im.ui.components.list.views :as list]
            [quo.components.list.item :as list-item]
            [status-im.ui.screens.chat.styles.photos :as photo-style]
            [reagent.core :as reagent]))

(def panel->icons {:extensions :main-icons/commands
                   :images     :main-icons/photo})

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

;; TODO(Ferossgp): Move this into audio panel.
;; Instead of not changing panel we can show a placeholder with no permission
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
    (if (= active panel)
      [icons/icon :main-icons/keyboard (styles/icon false)]
      [icons/icon :main-icons/speech (styles/icon false)])]])

(defn send-button [{:keys [on-send-press]}]
  [pressable/pressable {:type     :scale
                        :on-press on-send-press}
   [rn/view {:style (styles/send-message-button)}
    [icons/icon :main-icons/arrow-up
     {:container-style     (styles/send-message-container)
      :accessibility-label :send-message-button
      :color               (styles/send-icon-color)}]]])

(defn text-input
  [{:keys [cooldown-enabled? input-with-mentions on-text-change set-active-panel text-input-ref]}]
  (let [cursor            @(re-frame/subscribe [:chat/cursor])
        mentionable-users @(re-frame/subscribe [:chats/mentionable-users])]
    [rn/view {:style (styles/text-input-wrapper)}
     [rn/text-input
      {:style                  (styles/text-input)
       :ref                    text-input-ref
       :maxFontSizeMultiplier  1
       :accessibility-label    :chat-message-input
       :text-align-vertical    :center
       :multiline              true
       :editable               (not cooldown-enabled?)
       :blur-on-submit         false
       :auto-focus             false
       :on-focus               #(set-active-panel nil)
       :max-length             chat.constants/max-text-size
       :placeholder-text-color (:text-02 @colors/theme)
       :placeholder            (if cooldown-enabled?
                                 (i18n/label :cooldown/text-input-disabled)
                                 (i18n/label :t/type-a-message))
       :underlineColorAndroid  :transparent
       :auto-capitalize        :sentences
       :selection
       ;; NOTE(rasom): In case if mention is added on pressing suggestion and
       ;; it is placed inside some text we have to specify `:selection` on
       ;; Android to ensure that cursor is added after the mention, not after
       ;; the last char in input. On iOS it works that way without this code
       (when (and cursor platform/android?)
         (clj->js
          {:start cursor
           :end   cursor}))

       :on-selection-change
       (fn [_]
         ;; NOTE(rasom): we have to reset `cursor` value when user starts using
         ;; text-input because otherwise cursor will stay in the same position
         (when (and cursor platform/android?)
           (re-frame/dispatch [::mentions/clear-cursor])))

       :on-change
       (fn [args]
         (let [text (.-text ^js (.-nativeEvent ^js args))]
           (on-text-change text)
           ;; NOTE(rasom): on iOS `on-change` is dispatched after `on-text-input`,
           ;; that's why mention suggestions are calculated on `on-change`
           (when platform/ios?
             (re-frame/dispatch [::mentions/calculate-suggestions mentionable-users]))))

       :on-text-input
       (fn [args]
         (let [native-event  (.-nativeEvent ^js args)
               text          (.-text ^js native-event)
               previous-text (.-previousText ^js native-event)
               range         (.-range ^js native-event)
               start         (.-start ^js range)
               end           (.-end ^js range)]
           (re-frame/dispatch
            [::mentions/on-text-input
             {:new-text      text
              :previous-text previous-text
              :start         start
              :end           end}])
           ;; NOTE(rasom): on Android `on-text-input` is dispatched after
           ;; `on-change`, that's why mention suggestions are calculated
           ;; on `on-change`
           (when platform/android?
             (re-frame/dispatch [::mentions/calculate-suggestions mentionable-users]))))}
      ;; NOTE(rasom): reduce was used instead of for here because although
      ;; each text component was given a unique id it still would mess with
      ;; colors on Android. In case if entire component is built without lists
      ;; inside it works just fine on both platforms.
      (reduce
       (fn [acc [type text]]
         (conj
          acc
          [rn/text (when (= type :mention)
                     {:style {:color "#0DA4C9"}})
           text]))
       [:<>]
       input-with-mentions)]]))

(defn mention-item
  [[_ {:keys [identicon alias name] :as user}]]
  (let [title name
        subtitle? (not= alias name)]
    [list-item/list-item
     (cond-> {:icon
              [rn/view {:style {}}
               [rn/image
                {:source      {:uri identicon}
                 :style       (photo-style/photo-border
                               photo-style/default-size
                               nil)
                 :resize-mode :cover}]]
              :icon-container-style {}
              :size                 :small
              :text-size            :small
              :title                title
              :title-text-weight    :medium
              :on-press
              (fn []
                (re-frame/dispatch [:chat.ui/select-mention user]))}

       subtitle?
       (assoc :subtitle alias))]))

(def chat-input-height (reagent/atom nil))

(defn autocomplete-mentions []
  (let [suggestions @(re-frame/subscribe [:chat/mention-suggestions])]
    (when (and (seq suggestions) @chat-input-height)
      (let [height (+ 16 (* 52 (min 4.5 (count suggestions))))]
        [rn/view
         {:style (styles/autocomplete-container @chat-input-height)}
         [rn/view
          {:style {:height height}}
          [list/flat-list
           {:keyboardShouldPersistTaps :always
            :footer                    [rn/view {:style {:height 8}}]
            :header                    [rn/view {:style {:height 8}}]
            :data                      suggestions
            :key-fn                    first
            :render-fn                 #(mention-item %)}]]]))))

(defn chat-input
  [{:keys [set-active-panel active-panel on-send-press reply
           show-send show-image show-stickers show-extensions
           sending-image input-focus show-audio]
    :as   props}]
  [rn/view {:style (styles/toolbar)
            :on-layout #(reset! chat-input-height
                                (-> ^js % .-nativeEvent .-layout .-height))}
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
   [:<>
    ;; NOTE(rasom): on iOS `autocomplete-mentions` should be placed inside
    ;; `chat-input` (otherwise suggestions will be hidden by keyboard) but
    ;; outside animated view below because it adds horizontal margin 
    (when platform/ios?
      [autocomplete-mentions])
    [animated/view
     {:style (styles/input-container)}
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
                                :set-active          set-active-panel}])]]]]])

(defn chat-toolbar []
  (let [previous-layout          (atom nil)
        had-reply                (atom nil)]
    (fn [{:keys [active-panel set-active-panel text-input-ref on-text-change]}]
      (let [disconnected?        @(re-frame/subscribe [:disconnected?])
            {:keys [processing]} @(re-frame/subscribe [:multiaccounts/login])
            mainnet?             @(re-frame/subscribe [:mainnet?])
            input-text
            @(re-frame/subscribe [:chats/current-chat-input-text])
            input-with-mentions
            @(re-frame/subscribe [:chat/input-with-mentions])
            cooldown-enabled?    @(re-frame/subscribe [:chats/cooldown-enabled?])
            one-to-one-chat?     @(re-frame/subscribe [:current-chat/one-to-one-chat?])
            {:keys [public?
                    chat-id]}    @(re-frame/subscribe [:current-chat/metadata])
            reply                @(re-frame/subscribe [:chats/reply-message])
            sending-image        @(re-frame/subscribe [:chats/sending-image])
            input-focus          (fn []
                                   (some-> ^js (react/current-ref text-input-ref) .focus))
            clear-input          (fn []
                                   (some-> ^js (react/current-ref text-input-ref) .clear))
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
        [chat-input {:set-active-panel         set-active-panel
                     :active-panel             active-panel
                     :text-input-ref           text-input-ref
                     :input-focus              input-focus
                     :reply                    reply
                     :on-send-press            #(do (re-frame/dispatch [:chat.ui/send-current-message])
                                                    (clear-input))
                     :text-value               input-text
                     :input-with-mentions      input-with-mentions
                     :on-text-change           on-text-change
                     :cooldown-enabled?        cooldown-enabled?
                     :show-send                show-send
                     :show-stickers            show-stickers
                     :show-image               show-image
                     :show-audio               show-audio
                     :sending-image            sending-image
                     :show-extensions          show-extensions
                     :chat-id                  chat-id}]))))
