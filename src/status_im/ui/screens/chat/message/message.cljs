(ns status-im.ui.screens.chat.message.message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.constants :as constants]
            [status-im.utils.http :as http]
            [status-im.i18n :as i18n]
            [reagent.core :as reagent]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.security :as security]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.popup-menu.views :as desktop.pop-up]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.message.sheets :as sheets]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.styles.message.message :as style]
            [status-im.ui.screens.chat.utils :as chat.utils]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview message-content-command
  [command-message]
  (letsubs [id->command [:chats/id->command]
            {:keys [contacts]} [:chats/current-chat]]
    (let [{:keys [type] :as command} (commands-receiving/lookup-command-by-ref command-message id->command)]
      ;;TODO temporary disable commands for v1
      [react/text (str "Unhandled command: " (-> command-message :content :command-path first))])))

(defview message-timestamp
  [t justify-timestamp? outgoing command? content content-type]
  (when-not command?
    [react/text {:style (style/message-timestamp-text
                         justify-timestamp?
                         outgoing
                         (:rtl? content)
                         (= content-type constants/content-type-emoji))} t]))

(defn message-view
  [{:keys [timestamp-str outgoing content content-type] :as message} message-content {:keys [justify-timestamp?]}]
  [react/view (style/message-view message)
   message-content
   [message-timestamp timestamp-str justify-timestamp? outgoing (or (get content :command-path)
                                                                    (get content :command-ref))
    content content-type]])

(defview quoted-message [message-id {:keys [from text]} outgoing current-public-key]
  (letsubs [{:keys [quote
                    ens-name
                    alias]}
            [:messages/quote-info message-id]]
    (when (or quote text)
      [react/view {:style (style/quoted-message-container outgoing)}
       [react/view {:style style/quoted-message-author-container}
        [vector-icons/tiny-icon :tiny-icons/tiny-reply {:color (if outgoing colors/white-transparent colors/gray)}]
        (chat.utils/format-reply-author (or from (:from quote)) alias ens-name current-public-key (partial style/quoted-message-author outgoing))]

       [react/text {:style           (style/quoted-message-text outgoing)
                    :number-of-lines 5}
        (or text (:text quote))]])))

(defview message-content-status [{:keys [content]}]
  [react/view style/status-container
   [react/text {:style style/status-text}
    (:text content)]])

(defn expand-button [expanded? chat-id message-id]
  [react/text {:style    style/message-expand-button
               :on-press #(re-frame/dispatch [:chat.ui/message-expand-toggled chat-id message-id])}
   (i18n/label (if expanded? :show-less :show-more))])

(defn render-inline [message-text outgoing acc {:keys [type literal destination] :as node}]
  (case type
    ""
    (conj acc literal)

    "code"
    (conj acc [react/text-class style/inline-code-style literal])

    "emph"
    (conj acc [react/text-class (style/emph-style outgoing) literal])

    "strong"
    (conj acc [react/text-class (style/strong-style outgoing) literal])

    "link"
    (conj acc
          [react/text-class
           {:style
            {:color (if outgoing colors/white colors/blue)
             :text-decoration-line :underline}
            :on-press
            #(when (and (security/safe-link? destination)
                        (security/safe-link-text? message-text))
               (if platform/desktop?
                 (.openURL react/linking (http/normalize-url destination))
                 (re-frame/dispatch
                  [:browser.ui/message-link-pressed destination])))}
           destination])

    "status-tag"
    (conj acc [react/text-class
               {:style {:color (if outgoing colors/white colors/blue)
                        :text-decoration-line :underline}
                :on-press
                #(re-frame/dispatch
                  [:chat.ui/start-public-chat literal {:navigation-reset? true}])}
               "#"
               literal])

    (conj acc literal)))

(defn render-block [{:keys [chat-id message-id content
                            timestamp-str group-chat outgoing
                            current-public-key expanded?] :as message}
                    acc
                    {:keys [type literal children]}]
  (case type

    "paragraph"
    (conj acc (reduce
               (fn [acc e] (render-inline (:text content) outgoing acc e))
               [react/text-class (style/text-style outgoing)]
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

(defn render-parsed-text [{:keys [timestamp-str
                                  outgoing] :as message}

                          tree]
  (conj (reduce (fn [acc e] (render-block message acc e)) [react/view {}] tree)
        [react/text {:style (style/message-timestamp-placeholder outgoing)}
         (str "  " timestamp-str)]))

(defn text-message
  [{:keys [chat-id message-id content
           timestamp-str group-chat outgoing current-public-key expanded?] :as message}]
  [message-view message
   (let [response-to (:response-to content)]
     [react/view
      (when (seq response-to)
        [quoted-message response-to (:quoted-message message) outgoing current-public-key])
      [render-parsed-text message (:parsed-text content)]])
   {:justify-timestamp? true}])

(defn emoji-message
  [{:keys [content current-public-key alias] :as message}]
  (let [response-to (:response-to content)]
    [message-view message
     [react/view {:style (style/style-message-text false)}
      (when response-to
        [quoted-message response-to (:quoted-message message) alias false current-public-key])
      [react/text {:style (style/emoji-message message)}
       (:text content)]]]))

(defmulti message-content (fn [_ message _] (message :content-type)))

(defmethod message-content constants/content-type-text
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content constants/content-type-status
  [wrapper message]
  [wrapper message [message-content-status message]])

(defmethod message-content constants/content-type-command
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

;; Todo remove after couple of releases
(defmethod message-content constants/content-type-command-request
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

(defmethod message-content constants/content-type-emoji
  [wrapper message]
  [wrapper message [emoji-message message]])

(defmethod message-content constants/content-type-sticker
  [wrapper {:keys [content] :as message}]
  [wrapper message
   [react/image {:style {:margin 10 :width 140 :height 140}
                 :source {:uri (contenthash/url (:hash content))}}]])

(defmethod message-content :default
  [wrapper {:keys [content-type] :as message}]
  [wrapper message
   [message-view message
    [react/text (str "Unhandled content-type " content-type)]]])

(defn message-activity-indicator
  []
  [react/view style/message-activity-indicator
   [react/activity-indicator {:animating true}]])

(defn message-not-sent-text
  [chat-id message-id]
  [react/touchable-highlight
   {:on-press (fn [] (if platform/desktop?
                       (desktop.pop-up/show-desktop-menu
                        (desktop.pop-up/get-message-menu-items chat-id message-id))
                       (do
                         (re-frame/dispatch [:bottom-sheet/show-sheet
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

(defview command-status [{{:keys [network]} :params}]
  (letsubs [current-network [:chain-name]]
    (when (and network (not= current-network network))
      [react/view style/not-sent-view
       [react/text {:style style/not-sent-text}
        (i18n/label :network-mismatch)]
       [react/view style/not-sent-icon
        [vector-icons/icon :main-icons/warning {:color colors/red}]]])))

(defn message-delivery-status
  [{:keys [chat-id message-id outgoing-status
           first-outgoing?
           content message-type] :as message}]
  (when (not= :system-message message-type)
    (case outgoing-status
      :sending  [message-activity-indicator]
      :not-sent [message-not-sent-text chat-id message-id]
      :sent     (when first-outgoing?
                  [react/view style/delivery-view
                   [react/text {:style style/delivery-text}
                    (i18n/label :t/status-sent)]])
      (when (and (not outgoing-status)
                 (:command content))
        [command-status content]))))

(defview message-author-name [from alias]
  (letsubs [{:keys [ens-name]} [:contacts/contact-name-by-identity from]]
    (chat.utils/format-author alias style/message-author-name-container ens-name)))

(defn message-body
  [{:keys [alias
           last-in-group?
           first-in-group?
           display-photo?
           display-username?
           from
           outgoing
           modal?
           content] :as message} child]
  [react/view (style/group-message-wrapper message)
   [react/view (style/message-body message)
    (when display-photo?
      [react/view (style/message-author outgoing)
       (when first-in-group?
         [react/touchable-highlight {:on-press #(when-not modal? (re-frame/dispatch [:chat.ui/show-profile from]))}
          [react/view
           [photos/member-photo from]]])])
    [react/view (style/group-message-view outgoing display-photo?)
     (when display-username?
       [react/touchable-opacity {:on-press #(re-frame/dispatch [:chat.ui/show-profile from])}
        [message-author-name from alias]])
     [react/view {:style (style/timestamp-content-wrapper outgoing)}
      child]]]
   [react/view (style/delivery-status outgoing)
    [message-delivery-status message]]])

(defn open-chat-context-menu
  [{:keys [message-id content] :as message}]
  (list-selection/chat-message message-id (:text content) (i18n/label :t/message)))

(defn chat-message
  [{:keys [outgoing group-chat modal? current-public-key content-type content] :as message}]
  [react/view
   [react/touchable-highlight
    {:on-press      (fn [arg]
                      (if (and platform/desktop? (= "right" (.-button (.-nativeEvent arg))))
                        (open-chat-context-menu message)
                        (do
                          (when (and (= content-type constants/content-type-sticker) (:pack content))
                            (re-frame/dispatch [:stickers/open-sticker-pack (:pack content)]))
                          (re-frame/dispatch [:chat.ui/set-chat-ui-props {:messages-focused? true
                                                                          :input-bottom-sheet nil}])
                          (when-not platform/desktop?
                            (react/dismiss-keyboard!)))))
     :on-long-press #(when (or (= content-type constants/content-type-text)
                               (= content-type constants/content-type-emoji))
                       (open-chat-context-menu message))}
    [react/view {:accessibility-label :chat-item}
     (let [incoming-group (and group-chat (not outgoing))]
       [message-content message-body (merge message
                                            {:current-public-key current-public-key
                                             :group-chat         group-chat
                                             :modal?             modal?
                                             :incoming-group     incoming-group})])]]])
