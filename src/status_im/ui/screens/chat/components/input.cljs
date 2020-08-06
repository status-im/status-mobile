(ns status-im.ui.screens.chat.components.input
  (:require [status-im.ui.components.icons.vector-icons :as icons]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.platform :as platform]
            [quo.design-system.colors :as colors]
            [status-im.ui.screens.chat.components.style :as styles]
            [status-im.ui.screens.chat.components.reply :as reply]
            [status-im.utils.utils :as utils.utils]
            [status-im.ui.components.list.views :as list]
            [quo.components.list.item :as list-item]
            [status-im.ui.screens.chat.styles.photos :as photo-style]
            [status-im.chat.models.mentions :as mentions]
            [status-im.chat.models.input :as input]
            [status-im.ui.components.react :as components.react]
            [quo.components.animated.pressable :as pressable]
            [quo.animated :as animated]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [clojure.string :as string]
            [taoensso.timbre :as log]))

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

(defn input-part
  [[part-type part]]
  (log/info "part" part-type part)
  (case part-type
    :mention (if (string/includes? part " ")
               [:<>
                [rn/text {:style {:font-size 0}} "@"]
                [rn/text {:style {:color "#0DA4C9"}}
                 (str part " ")]]
               [rn/text {:style {:color "#0DA4C9"}}
                (str "@" part " ")])
    :text [rn/text part]
    :current [rn/text {:style {:color "#0DA4C9"}} part]
    [rn/text {} "Unkown"]))

(defn get-suggestions
  "Returns a sorted list of users matching the input"
  [users input]
  (when input
    (let [input (string/lower-case (subs input 1))]
      (->> (filter (fn [{:keys [alias]}]
                     (when (string? alias)
                       (-> alias
                           string/lower-case
                           (string/starts-with? input))))
                   users)
           (sort-by (comp string/lower-case :alias))
           seq))))

(defn text-input []
  (let [last-cursor (atom nil)]
    (fn [{:keys [cooldown-enabled? on-text-change
                 set-active-panel text-input-ref text-input-height-atom]
          {:keys [before-cursor after-cursor completing? cursor]} :input}]
      [rn/view {:style (styles/text-input-wrapper)
                :on-layout #(reset! text-input-height-atom
                                    (-> ^js % .-nativeEvent .-layout .-height))}
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
         :on-change              #(on-text-change (.-text ^js (.-nativeEvent ^js %)) @last-cursor)
         :placeholder-text-color (:text-02 @colors/theme)
         :placeholder            (if cooldown-enabled?
                                   (i18n/label :cooldown/text-input-disabled)
                                   (i18n/label :t/type-a-message))
         :underlineColorAndroid  :transparent
         :auto-capitalize        :sentences

         ;; NOTE: the auto-completion isn't going to trigger any
         ;; event of the text-input component
         ;; We manage the selection prop to reposition the cursor
         ;; and we reset the value here
         :selection
         (when cursor
           (clj->js {:start cursor
                     :end cursor}))
         :on-selection-change
         (fn [^js event]
           (let [^js selection (.-selection (.-nativeEvent event))
                 start (.-start selection)
                 end (.-end selection)]
             (if (= start end)
               (do
                 (reset! last-cursor start)
                 (re-frame/dispatch-sync
                  [::mentions/selection-change start]))
               (reset! last-cursor nil))))
         :on-key-press
         (fn [^js event]
           (let [key (.-key (.-nativeEvent event))]
             (when (= "@" key)
               (re-frame/dispatch-sync
                [::mentions/mention-pressed]))))}
        [:<>
         (for [[key part]
               (map-indexed vector before-cursor)]
           ^{:key (str key part)}
           [input-part part])
         (when completing?
           [input-part  completing?])
         (for [[key part]
               (map-indexed vector after-cursor)]
           ^{:key (str key part)}
           [input-part part])]]])))

(defn mention-item
  [{:keys [identicon alias public-key]}]
  [list-item/list-item
   {:icon [components.react/view {:style {:padding-horizontal 4}}
           [components.react/image
            {:source      {:uri identicon}
             :style       (photo-style/photo photo-style/default-size)
             :resize-mode :cover}]]
    :title alias
    :on-press (fn []
                (re-frame/dispatch [::mentions/complete-mention alias public-key]))}])

(defn autocomplete-mentions
  [suggestions !message-view-height message-view-width-atom text-input-height-atom]
  (when (seq suggestions)
    (let [height (min (or @!message-view-height 0)
                      (* 64 (count suggestions)))]
      (log/info "suggestions" height @!message-view-height)
      [components.react/view {:style (styles/autocomplete-container @text-input-height-atom)}
       ;;NOTE: a flatlist needs to be contained within a view with
       ;;a defined height in order to be scrollable
       ;;since the suggestions have to fill up to the entire space
       ;;used by the message-view, we get the height of that view
       ;;on layout and use it as a maximal height
       [components.react/view {:style {:height height
                                       :width  @message-view-width-atom}}
        [list/flat-list
         {:keyboardShouldPersistTaps :always
          :style                     {:background-color :white
                                      :border-radius    1
                                      :shadow-radius    12
                                      :shadow-opacity   0.16
                                      :shadow-color     "rgba(0, 0, 0, 0.12)"}
          :data                      suggestions
          :key-fn                    identity
          :inverted                  true
          :render-fn                 #(mention-item %)}]]])))

(defn chat-input []
  (let [text-input-height-atom (atom 0)]
    (fn [{:keys [set-active-panel active-panel on-send-press reply
                 show-send show-image show-stickers show-extensions input
                 sending-image input-focus show-audio message-view-height-atom message-view-width-atom]
          :as   props}]
      (log/info "redraw chat-input")
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
       [:<>
        [components.react/view {}
         [autocomplete-mentions (:suggestions input) message-view-height-atom message-view-width-atom text-input-height-atom]]
        [animated/view {:style (styles/input-container)}
         (when reply
           [reply/reply-message reply])
         (when sending-image
           [reply/send-image sending-image])
         [rn/view {:style (styles/input-row)}
          [text-input (assoc props :text-input-height-atom text-input-height-atom)]
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
                                    :set-active          set-active-panel}])]]]]])))

(defn chat-toolbar []
  (let [previous-layout (atom nil)
        had-reply       (atom nil)]
    (fn [{:keys [active-panel set-active-panel text-input-ref
                 message-view-height-atom message-view-width-atom]}]
      (let [disconnected?        @(re-frame/subscribe [:disconnected?])
            {:keys [processing]} @(re-frame/subscribe [:multiaccounts/login])
            mainnet?             @(re-frame/subscribe [:mainnet?])
            {:keys [input-text]
             :as   input}        @(re-frame/subscribe [:chats/current-chat-input])
            cooldown-enabled?    @(re-frame/subscribe [:chats/cooldown-enabled?])
            one-to-one-chat?     @(re-frame/subscribe [:current-chat/one-to-one-chat?])
            public?              @(re-frame/subscribe [:current-chat/public?])
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
        (log/info "redraw chat-toolbar")
        [chat-input
         {:set-active-panel         set-active-panel
          :active-panel             active-panel
          :text-input-ref           text-input-ref
          :input-focus              input-focus
          :reply                    reply
          :on-send-press            #(do (re-frame/dispatch [::input/send-current-message-pressed])
                                         (clear-input))
          :text-value               input-text
          :input                    input
          :on-text-change           #(re-frame/dispatch [::input/input-text-changed %1 %2])
          :cooldown-enabled?        cooldown-enabled?
          :show-send                show-send
          :show-stickers            show-stickers
          :show-image               show-image
          :show-audio               show-audio
          :sending-image            sending-image
          :show-extensions          show-extensions
          :message-view-height-atom message-view-height-atom
          :message-view-width-atom  message-view-width-atom}]))))
