(ns status-im.ui.screens.chat.message.message
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.utils.http :as http]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.security :as security]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.popup-menu.views :as desktop.pop-up]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.styles.message.message :as style]
            [status-im.ui.screens.chat.utils :as chat.utils]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.chat.message.command :as message.command])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview mention-element [from]
  (letsubs [{:keys [ens-name alias]} [:contacts/contact-name-by-identity from]]
    (if ens-name (str "@" ens-name) alias)))

(defn message-timestamp
  ([message]
   [message-timestamp message false])
  ([{:keys [timestamp-str outgoing content]} justify-timestamp?]
   [react/text {:style (style/message-timestamp-text
                        justify-timestamp?
                        outgoing
                        (:rtl? content))} timestamp-str]))

(defn message-bubble-wrapper
  [message message-content appender]
  [react/view
   (style/message-view message)
   message-content
   appender])

(defview quoted-message
  [message-id {:keys [from text]} outgoing current-public-key]
  (letsubs [{:keys [ens-name alias]} [:contacts/contact-name-by-identity from]]
    [react/view {:style (style/quoted-message-container outgoing)}
     [react/view {:style style/quoted-message-author-container}
      [chat.utils/format-reply-author
       from
       alias
       ens-name
       current-public-key
       (partial style/quoted-message-author outgoing)]]

     [react/text {:style           (style/quoted-message-text outgoing)
                  :number-of-lines 5}
      text]]))

(defn render-inline [message-text outgoing content-type acc {:keys [type literal destination]}]
  (case type
    ""
    (conj acc literal)

    "code"
    (conj acc [react/text-class (style/inline-code-style) literal])

    "emph"
    (conj acc [react/text-class (style/emph-style outgoing) literal])

    "strong"
    (conj acc [react/text-class (style/strong-style outgoing) literal])

    "link"
    (conj acc
          [react/text-class
           {:style
            {:color (if outgoing colors/white-persist colors/blue)
             :text-decoration-line :underline}
            :on-press
            #(when (and (security/safe-link? destination)
                        (security/safe-link-text? message-text))
               (if platform/desktop?
                 (.openURL react/linking (http/normalize-url destination))
                 (re-frame/dispatch
                  [:browser.ui/message-link-pressed destination])))}
           destination])

    "mention"
    (conj acc [react/text-class
               {:style {:color (cond
                                 (= content-type constants/content-type-system-text) colors/black
                                 outgoing colors/mention-outgoing
                                 :else colors/mention-incoming)}
                :on-press (when-not (= content-type constants/content-type-system-text)
                            #(re-frame/dispatch [:chat.ui/start-chat literal {:navigation-reset? true}]))}
               [mention-element literal]]) "status-tag"
    (conj acc [react/text-class
               {:style {:color (if outgoing colors/white-persist colors/blue)
                        :text-decoration-line :underline}
                :on-press
                #(re-frame/dispatch
                  [:chat.ui/start-public-chat literal {:navigation-reset? true}])}
               "#"
               literal])

    (conj acc literal)))

(defview message-content-status [{:keys [content content-type]}]
  [react/view style/status-container
   [react/text {:style (style/status-text)}
    (reduce
     (fn [acc e] (render-inline (:text content) false content-type acc e))
     [react/text-class {:style (style/status-text)}]
     (-> content :parsed-text peek :children))]])

(defn render-block [{:keys [content outgoing content-type]} acc
                    {:keys [type literal children]}]
  (case type

    "paragraph"
    (conj acc (reduce
               (fn [acc e] (render-inline (:text content) outgoing content-type acc e))
               [react/text-class (style/text-style outgoing content-type)]
               children))

    "blockquote"
    (conj acc [react/view (style/blockquote-style outgoing)
               [react/text-class (style/blockquote-text-style outgoing)
                (.substring literal 0 (dec (.-length literal)))]])

    "codeblock"
    (conj acc [react/view style/codeblock-style
               [react/text-class style/codeblock-text-style
                (.substring literal 0 (dec (.-length literal)))]])

    acc))

(defn render-parsed-text [message tree]
  (reduce (fn [acc e] (render-block message acc e)) [react/view {}] tree))

(defn render-parsed-text-with-timestamp [{:keys [timestamp-str outgoing] :as message} tree]
  (let [elements (render-parsed-text message tree)
        timestamp [react/text {:style (style/message-timestamp-placeholder outgoing)}
                   (str "  " timestamp-str)]
        last-element (peek elements)]
    ;; Using `nth` here as slightly faster than `first`, roughly 30%
    ;; It's worth considering pure js structures for this code path as
    ;; it's perfomance critical
    (if (= react/text-class (nth last-element 0))
      ;; Append timestamp to last text
      (conj (pop elements) (conj last-element timestamp))
      ;; Append timestamp to new block
      (conj elements timestamp))))

(defn text-message
  [{:keys [content outgoing current-public-key] :as message}]
  [message-bubble-wrapper message
   (let [response-to (:response-to content)]
     [react/view
      (when (and (seq response-to) (:quoted-message message))
        [quoted-message response-to (:quoted-message message) outgoing current-public-key])
      [render-parsed-text-with-timestamp message (:parsed-text content)]])
   [message-timestamp message true]])

(defn system-text-message
  [{:keys [content] :as message}]
  [message-bubble-wrapper message
   [react/view
    [render-parsed-text message (:parsed-text content)]]])

(defn emoji-message
  [{:keys [content current-public-key alias outgoing] :as message}]
  (let [response-to (:response-to content)]
    [message-bubble-wrapper message
     [react/view {:style (style/style-message-text outgoing)}
      (when response-to
        [quoted-message response-to (:quoted-message message) alias outgoing current-public-key])
      [react/text {:style (style/emoji-message message)}
       (:text content)]]
     [message-timestamp message]]))

(defn message-activity-indicator
  []
  [react/view style/message-activity-indicator
   [react/activity-indicator {:animating true}]])

(defn message-not-sent-text
  [chat-id message-id]
  [react/touchable-highlight
   {:on-press
    (fn [] (if platform/desktop?
             (desktop.pop-up/show-desktop-menu
              (desktop.pop-up/get-message-menu-items chat-id message-id))
             (do
               (re-frame/dispatch
                [:bottom-sheet/show-sheet
                 {:content        (sheets/options chat-id message-id)
                  :content-height 200}])
               (react/dismiss-keyboard!))))}
   [react/view style/not-sent-view
    [react/text {:style style/not-sent-text}
     (i18n/label (if platform/desktop?
                   :t/status-not-sent-click
                   :t/status-not-sent-tap))]
    [react/view style/not-sent-icon
     [vector-icons/icon :main-icons/warning {:color colors/red}]]]])

(defn message-delivery-status
  [{:keys [chat-id message-id outgoing-status
           first-outgoing? message-type]}]
  (when (not= constants/message-type-private-group-system-message message-type)
    (case outgoing-status
      :sending  [message-activity-indicator]
      :not-sent [message-not-sent-text chat-id message-id]
      :sent     (when first-outgoing?
                  [react/text {:style style/delivery-text}
                   (i18n/label :t/status-sent)])
      nil)))

(defview message-author-name [from alias]
  (letsubs [{:keys [ens-name]} [:contacts/contact-name-by-identity from]]
    (chat.utils/format-author alias style/message-author-name-container ens-name)))

(defn message-press-handlers [{:keys [outgoing from content-type content] :as message}]
  (let [pack (get-in content [:sticker :pack])]
    {:on-press      (fn [_]
                      (when (and (= content-type constants/content-type-sticker) pack)
                        (re-frame/dispatch [:stickers/open-sticker-pack pack]))
                      (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-bottom-sheet nil}])
                      (react/dismiss-keyboard!))
     :on-long-press #(cond (or (= content-type constants/content-type-text)
                               (= content-type constants/content-type-emoji))
                           (re-frame/dispatch [:bottom-sheet/show-sheet
                                               {:content (sheets/message-long-press message)
                                                :height  192}])
                           (and (= content-type constants/content-type-sticker)
                                from (not outgoing))
                           (re-frame/dispatch [:bottom-sheet/show-sheet
                                               {:content (sheets/sticker-long-press message)
                                                :height  64}]))}))

(defn message-content-wrapper
  "Author, userpic and delivery wrapper"
  [{:keys [alias first-in-group? display-photo? identicon display-username?
           from outgoing]
    :as   message} content]
  [react/view {:style (style/message-wrapper message)
               :accessibility-label :chat-item}
   [react/view (style/message-body message)
    (when display-photo?
      ; userpic
      [react/view (style/message-author-userpic outgoing)
       (when first-in-group?
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/show-profile from])}
          [photos/member-identicon identicon]])])
    ; username
    [react/view (style/message-author-wrapper outgoing display-photo?)
     (when display-username?
       [react/touchable-opacity {:style    style/message-author-touchable
                                 :on-press #(re-frame/dispatch [:chat.ui/show-profile from])}
        ;;TODO (perf) move to event
        [message-author-name from alias]])
     ;;MESSAGE CONTENT
     content]]
   ; delivery status
   [react/view (style/delivery-status outgoing)
    [message-delivery-status message]]])

(defn system-message-content-wrapper
  [message child]
  [react/view {:style (style/message-wrapper-base message)
               :accessibility-label :chat-item}
   [react/view (style/system-message-body message)
    [react/view child]]])

(defn chat-message [{:keys [content content-type] :as message}]
  (if (= content-type constants/content-type-command)
    [message.command/comand-content message-content-wrapper message]
    (if (= content-type constants/content-type-system-text)
      [system-message-content-wrapper message [system-text-message message]]
      [react/touchable-highlight (message-press-handlers message)
       [message-content-wrapper
        message
        (if (= content-type constants/content-type-text)
          ; text message
          [text-message message]
          (if (= content-type constants/content-type-status)
            [message-content-status message]
            (if (= content-type constants/content-type-emoji)
              [emoji-message message]
              (if (= content-type constants/content-type-sticker)
                [react/image {:style  {:margin 10 :width 140 :height 140}
                              ;;TODO (perf) move to event
                              :source {:uri (contenthash/url (-> content :sticker :hash))}}]
                [message-bubble-wrapper message
                 [react/text (str "Unhandled content-type " content-type)]]))))]])))
