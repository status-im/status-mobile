(ns status-im2.contexts.chat.messages.content.pin.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.chat.composer.reply.view :as reply]
            [status-im2.contexts.chat.messages.content.pin.style :as style]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [status-im.ui2.screens.chat.messages.message :as old-message]
            [status-im.ui.screens.chat.styles.message.message :as old-style]))

(defn pinned-by-view
  [pinned-by]
  (let [{:keys [public-key]} (rf/sub [:multiaccount/contact])
        contact-names        (rf/sub [:contacts/contact-two-names-by-identity pinned-by])
        author-name          (if (= pinned-by public-key) (i18n/label :t/You) (first contact-names))]
    [rn/view
     {:style               style/pin-indicator-container
      :accessibility-label :pinned-by}
     [quo/icon :i/pin {:color colors/primary-50 :size 16}]
     [quo/text
      {:size   :label
       :weight :medium
       :style  style/pin-author-text}
      author-name]]))

(defn system-message
  [{:keys [from in-popover? quoted-message timestamp-str chat-id] :as message}]
  [rn/touchable-opacity
   {:on-press       (fn []
                      (rf/dispatch [:dismiss-keyboard])
                      (rf/dispatch [:pin-message/show-pins-bottom-sheet chat-id]))
    :active-opacity 1
    :style          (merge style/system-message-container
                           (old-style/message-wrapper message))}
   [rn/view
    {:style               style/system-message-inner-container
     :accessibility-label :content-type-pin-icon}
    [quo/icon :i/pin {:color colors/primary-50 :size 16}]]
   [rn/view
    [rn/view {:style style/system-message-author-container}
     [rn/touchable-opacity
      {:style    old-style/message-author-touchable
       :disabled in-popover?
       :on-press #(rf/dispatch [:chat.ui/show-profile from])}
      [old-message/message-author-name from {} 20]]
     [quo/text
      {:size  :label
       :style (style/pinned-message-text)}
      (str " " (i18n/label :t/pinned-a-message))]
     [rn/text
      {:style               (merge
                             style/system-message-timestamp-container
                             (old-style/message-timestamp-text))
       :accessibility-label :message-timestamp}
      timestamp-str]]
    [reply/quoted-message quoted-message false true]]])
