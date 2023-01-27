(ns status-im2.contexts.chat.messages.content.pin.view
  (:require [react-native.core :as rn]
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
  [{:keys [from in-popover? timestamp-str chat-id] :as message}]
  (let [response-to  (:response-to (:content message))
        default-size 36]
    [rn/touchable-opacity
     {:on-press       #(do
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:bottom-sheet/show-sheet :pinned-messages-list chat-id]))
      :active-opacity 1
      :style          (merge {:flex-direction :row :margin-vertical 8}
                             (old-style/message-wrapper message))}
     [rn/view
      {:style               {:width            default-size
                             :height           default-size
                             :margin-right     16
                             :border-radius    default-size
                             :justify-content  :center
                             :align-items      :center
                             :background-color colors/primary-50-opa-10}
       :accessibility-label :content-type-pin-icon}
      [quo/icon :i/pin {:color colors/primary-50 :size 16}]]
     [rn/view
      [rn/view {:style {:flex-direction :row :align-items :center}}
       [rn/touchable-opacity
        {:style    old-style/message-author-touchable
         :disabled in-popover?
         :on-press #(rf/dispatch [:chat.ui/show-profile from])}
        [old-message/message-author-name from {} 20]]
       [rn/text {:style {:font-size 13}} (str " " (i18n/label :t/pinned-a-message))]
       [rn/text
        {:style               (merge
                               {:padding-left 5
                                :margin-top   2}
                               (old-style/message-timestamp-text))
         :accessibility-label :message-timestamp}
        timestamp-str]]
      [old-message/quoted-message {:message-id response-to :chat-id chat-id} (:quoted-message message)
       true]]]))
