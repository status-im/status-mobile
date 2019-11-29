(ns status-im.ui.screens.chat.input.input
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.chat.styles.input.input :as style]
            [status-im.ui.screens.chat.styles.message.message :as message-style]
            [status-im.ui.screens.chat.input.send-button :as send-button]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.utils :as chat-utils]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.utils.config :as config]
            [status-im.ui.screens.chat.image.views :as image]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.extensions.views :as extensions]))

(defn basic-text-input [input-text cooldown-enabled?]
  [react/text-input
   {:ref                    #(when % (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-ref %}]))
    :accessibility-label    :chat-message-input
    :multiline              true
    :default-value          (or input-text "")
    :editable               (not cooldown-enabled?)
    :blur-on-submit         false
    :on-focus               #(re-frame/dispatch-sync [:chat.ui/input-on-focus])
    :on-change              #(re-frame/dispatch [:chat.ui/set-chat-input-text (.-text ^js (.-nativeEvent ^js %))])
    :style                  style/input-view
    :placeholder            (if cooldown-enabled?
                              (i18n/label :cooldown/text-input-disabled)
                              (i18n/label :t/type-a-message))
    :placeholder-text-color colors/gray
    :auto-capitalize        :sentences}])

(defview reply-message [from alias message-text]
  (letsubs [{:keys [ens-name]} [:contacts/contact-name-by-identity from]
            current-public-key [:multiaccount/public-key]]
    [react/scroll-view {:style style/reply-message-content}
     [react/view {:style style/reply-message-to-container}
      (chat-utils/format-reply-author from alias ens-name current-public-key style/reply-message-author)]
     [react/text {:style (assoc (message-style/style-message-text false) :font-size 14) :number-of-lines 3} message-text]]))

(defview reply-message-view []
  (letsubs [{:keys [content from alias] :as message} [:chats/reply-message]]
    (when message
      [react/view {:style style/reply-message}
       [photos/member-photo from]
       [reply-message from alias (:text content)]
       [react/touchable-highlight
        {:style               style/cancel-reply-highlight
         :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
         :accessibility-label :cancel-message-reply}
        [react/view {:style style/cancel-reply-container}
         [vector-icons/icon :main-icons/close {:container-style style/cancel-reply-icon
                                               :width           19
                                               :height          19
                                               :color           colors/white}]]]])))

(defview container []
  (letsubs [mainnet?           [:mainnet?]
            input-text         [:chats/current-chat-input-text]
            cooldown-enabled?  [:chats/cooldown-enabled?]
            input-bottom-sheet [:chats/current-chat-ui-prop :input-bottom-sheet]
            one-to-one-chat?   [:current-chat/one-to-one-chat?]]
    (let [input-text-empty? (string/blank? (string/trim (or input-text "")))]
      [react/view {:style (style/root)}
       [reply-message-view]
       [react/view {:style style/input-container}
        [basic-text-input input-text cooldown-enabled?]
        (when input-text-empty?
          [image/button (= :images input-bottom-sheet)])
        (when (and input-text-empty? mainnet?)
          [stickers/button (= :stickers input-bottom-sheet)])
        (when (and one-to-one-chat? input-text-empty? (or config/commands-enabled? mainnet?))
          [extensions/button (= :extensions input-bottom-sheet)])
        (when-not input-text-empty?
          [send-button/send-button-view input-text-empty?
           #(re-frame/dispatch [:chat.ui/send-current-message])])]])))
