(ns status-im.ui.screens.chat.message.message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.popup-menu.views :as desktop.pop-up]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.styles.message.message :as style]
            [status-im.ui.screens.chat.utils :as chat.utils]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.platform :as platform]
            [status-im.utils.config :as config])
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

(defview quoted-message [{:keys [from text]} outgoing current-public-key]
  (letsubs [username [:contacts/contact-name-by-identity from]]
    [react/view {:style (style/quoted-message-container outgoing)}
     [react/view {:style style/quoted-message-author-container}
      [vector-icons/tiny-icon :tiny-icons/tiny-reply {:color (if outgoing colors/white-transparent colors/gray)}]
      (chat.utils/format-reply-author from username current-public-key (partial style/quoted-message-author outgoing))]

     [react/text {:style           (style/quoted-message-text outgoing)
                  :number-of-lines 5}
      text]]))

(defview message-content-status [{:keys [content]}]
  [react/view style/status-container
   [react/text {:style style/status-text}
    (:text content)]])

(defn expand-button [expanded? chat-id message-id]
  [react/text {:style    style/message-expand-button
               :on-press #(re-frame/dispatch [:chat.ui/message-expand-toggled chat-id message-id])}
   (i18n/label (if expanded? :show-less :show-more))])

(defn text-message
  [{:keys [chat-id message-id content timestamp-str group-chat outgoing current-public-key expanded?] :as message}]
  [message-view message
   (let [collapsible? (and (:should-collapse? content) group-chat)]
     [react/view
      (when (:response-to content)
        [quoted-message (:response-to content) outgoing current-public-key])
      (apply react/nested-text
             (cond-> {:style (style/text-message collapsible? outgoing)
                      :text-break-strategy :balanced
                      :parseBasicMarkdown true
                      :markdownCodeBackgroundColor colors/black
                      :markdownCodeForegroundColor colors/green}

               (and collapsible? (not expanded?))
               (assoc :number-of-lines constants/lines-collapse-threshold))
             (conj (if-let [render-recipe (:render-recipe content)]
                     (chat.utils/render-chunks render-recipe message)
                     [(:text content)])
                   [{:style (style/message-timestamp-placeholder outgoing)}
                    (str "  " timestamp-str)]))

      (when collapsible?
        [expand-button expanded? chat-id message-id])])
   {:justify-timestamp? true}])

(defn emoji-message
  [{:keys [content current-public-key] :as message}]
  [message-view message
   [react/view {:style (style/style-message-text false)}
    (when (:response-to content)
      [quoted-message (:response-to content) false current-public-key])
    [react/text {:style (style/emoji-message message)}
     (:text content)]]])

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

(defn- text-status [status]
  [react/view style/delivery-view
   [react/text {:style style/delivery-text}
    (i18n/message-status-label status)]])

(defn message-activity-indicator
  []
  [react/view style/message-activity-indicator
   [react/activity-indicator {:animating true}]])

(defn message-not-sent-text
  [chat-id message-id]
  [react/touchable-highlight
   {:on-press (fn [] (cond
                       platform/ios?
                       (action-sheet/show
                        {:title   (i18n/label :message-not-sent)
                         :options [{:label  (i18n/label :resend-message)
                                    :action #(re-frame/dispatch
                                              [:chat.ui/resend-message chat-id message-id])}
                                   {:label        (i18n/label :delete-message)
                                    :destructive? true
                                    :action       #(re-frame/dispatch
                                                    [:chat.ui/delete-message chat-id message-id])}]})
                       platform/desktop?
                       (desktop.pop-up/show-desktop-menu
                        (desktop.pop-up/get-message-menu-items chat-id message-id))

                       :else
                       (re-frame/dispatch
                        [:chat.ui/show-message-options {:chat-id    chat-id
                                                        :message-id message-id}])))}
   [react/view style/not-sent-view
    [react/text {:style style/not-sent-text}
     (i18n/message-status-label (if platform/desktop?
                                  :not-sent-click
                                  :not-sent-tap))]
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
           content last-outgoing? message-type] :as message}]
  (when (not= :system-message message-type)
    (case outgoing-status
      :sending  [message-activity-indicator]
      :not-sent [message-not-sent-text chat-id message-id]
      (if (and (not outgoing-status)
               (:command content))
        [command-status content]
        (when last-outgoing?
          (if outgoing-status
            [text-status outgoing-status]))))))

(defview message-author-name [from name]
  (letsubs [username [:contacts/contact-name-by-identity from]]
    (chat.utils/format-author from style/message-author-name name)))

(defn message-body
  [{:keys [last-in-group?
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
       (when last-in-group?
         [react/touchable-highlight {:on-press #(when-not modal? (re-frame/dispatch [:chat.ui/show-profile from]))}
          [react/view
           [photos/member-photo from]]])])
    [react/view (style/group-message-view outgoing display-photo?)
     (when display-username?
       [react/touchable-opacity {:on-press #(re-frame/dispatch [:chat.ui/show-profile from])}
        [message-author-name from (:name content)]])
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
                                                                          :show-stickers?    false}])
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
