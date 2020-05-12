(ns status-im.ui.screens.chat.input.input
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.chat.models.input :as chat-input]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.typography :as typography]
            [status-im.ui.screens.chat.extensions.views :as extensions]
            [status-im.ui.screens.chat.input.send-button :as send-button]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.styles.input.input :as style]
            [status-im.ui.screens.chat.styles.message.message :as message-style]
            [status-im.ui.screens.chat.utils :as chat-utils]
            [status-im.utils.config :as config])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn input-part
  [[part-type part]]
  (case part-type
    :mention (if (string/includes? part " ")
               [:<>
                [react/text {:style {:font-size 0}} "@"]
                [react/text {:style {:color "#0DA4C9"}}
                 (str part " ")]]
               [react/text {:style {:color "#0DA4C9"}}
                (str "@" part " ")])
    :text [react/text part]
    :current [react/text {:style {:color "#0DA4C9"}} part]
    [react/text {} "Unkown"]))

(defn basic-text-input
  [{:keys [before-cursor after-cursor completing? cursor]} cooldown-enabled?]
  [react/text-input-class
   {:ref
    #(when % (re-frame/dispatch
              [:chat.ui/set-chat-ui-props {:input-ref %}]))
    :accessibility-label :chat-message-input
    :multiline true
    :editable (not cooldown-enabled?)
    :blur-on-submit false
    :on-focus
    #(re-frame/dispatch-sync
      [:chat.ui/input-on-focus])
    :on-change-text
    (fn [new-value]
      (re-frame/dispatch-sync
       [::chat-input/input-text-changed new-value]))
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
        (when (= start end)
          (re-frame/dispatch-sync
           [::chat-input/selection-change start]))))
    :on-key-press
    (fn [^js event]
      (let [key (.-key (.-nativeEvent event))]
        (when (= "@" key)
          (re-frame/dispatch-sync
           [::chat-input/mention-pressed]))))
    :style (typography/get-style style/input-view)
    :placeholder (if cooldown-enabled?
                   (i18n/label :cooldown/text-input-disabled)
                   (i18n/label :t/type-a-message))
    :placeholder-text-color colors/gray
    :auto-capitalize        :sentences
    :underline-color-android  :transparent
    :max-font-size-multiplier react/max-font-size-multiplier}
   [:<>
    (for [[key part]
          (map-indexed vector before-cursor)]
      ^{:key (str key part)}
      [input-part part])
    (when completing?
      [input-part completing?])
    (for [[key part]
          (map-indexed vector after-cursor)]
      ^{:key (str key part)}
      [input-part part])]])

(defview reply-message [from alias message-text]
  (letsubs [{:keys [ens-name]} [:contacts/contact-name-by-identity from]
            current-public-key [:multiaccount/public-key]]
    [react/scroll-view {:style style/reply-message-content}
     [react/view {:style style/reply-message-to-container}
      (chat-utils/format-reply-author from
                                      alias
                                      ens-name
                                      current-public-key
                                      style/reply-message-author)]
     [react/text {:style (assoc (message-style/style-message-text false)
                                :font-size 14)
                  :number-of-lines 3}
      message-text]]))

(defview reply-message-view []
  (letsubs [{:keys [content from alias] :as message}
            [:chats/reply-message]]
    (when message
      [react/view {:style style/reply-message}
       [photos/member-photo from]
       [reply-message from alias (:text content)]
       [react/touchable-highlight
        {:style               style/cancel-reply-highlight
         :on-press            #(re-frame/dispatch
                                [:chat.ui/cancel-message-reply])
         :accessibility-label :cancel-message-reply}
        [react/view {:style style/cancel-reply-container}
         [vector-icons/icon
          :main-icons/close
          {:container-style style/cancel-reply-icon
           :width           19
           :height          19
           :color           colors/white}]]]])))

(defn mention-item
  [{:keys [identicon alias public-key]}]
  [list-item/list-item
   {:icon identicon
    :title alias
    :on-press #(re-frame/dispatch [::chat-input/complete-mention alias public-key])}])

(defn autocomplete-mentions
  [suggestions !message-view-height]
  (when suggestions
    [react/view {:style style/autocomplete-container}
     ;;NOTE: a flatlist needs to be contained within a view with
     ;;a defined height in order to be scrollable
     ;;since the suggestions have to fill up to the entire space
     ;;used by the message-view, we get the height of that view
     ;;on layout and use it as a maximal height
     [react/view {:style {:height (min (or @!message-view-height 0)
                                       (* 64 (count suggestions)))}}
      [list/flat-list {:keyboardShouldPersistTaps :always
                       :style  {:background-color :white
                                :border-radius      1
                                :shadow-radius      12
                                :shadow-opacity     0.16
                                :shadow-color       "rgba(0, 0, 0, 0.12)"}
                       :data suggestions
                       :key-fn identity
                       :inverted true
                       :render-fn #(mention-item %)}]]]))

(defview container [!message-view-height]
  (letsubs [mainnet?           [:mainnet?]
            cooldown-enabled?  [:chats/cooldown-enabled?]
            input-bottom-sheet [:chats/current-chat-ui-prop :input-bottom-sheet]
            one-to-one-chat?   [:current-chat/one-to-one-chat?]
            input              [:chats/current-chat-input]]
    (let [{:keys [input-text-empty? suggestions]} input]
      [:<>
       [react/view {}
        [autocomplete-mentions suggestions !message-view-height]]
       [react/view {:style (style/root)}
        [reply-message-view]
        [react/view {:style style/input-container}
         [basic-text-input input cooldown-enabled?]
         (when (and input-text-empty? mainnet?)
           [stickers/button (= :stickers input-bottom-sheet)])
         (when (and one-to-one-chat? input-text-empty? (or config/commands-enabled? mainnet?))
           [extensions/button (= :extensions input-bottom-sheet)])
         [send-button/send-button-view input-text-empty?
          #(re-frame/dispatch [::chat-input/send-current-message-pressed])]]]])))
