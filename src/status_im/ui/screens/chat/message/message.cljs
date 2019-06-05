(ns status-im.ui.screens.chat.message.message
  (:require [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.protocol :as protocol]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.constants :as constants]
            [status-im.extensions.core :as extensions]
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
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn install-extension-message [extension-id outgoing]
  [react/touchable-highlight {:on-press #(re-frame/dispatch
                                          [:extensions.ui/install-extension-button-pressed extension-id])}
   [react/view style/extension-container
    [vector-icons/icon :main-icons/info {:color (if outgoing colors/white colors/gray)}]
    [react/text {:style (style/extension-text outgoing)}
     (i18n/label :to-see-this-message)]
    [react/text {:style (style/extension-install outgoing)}
     (i18n/label :install-the-extension)]]])

(defn message-timestamp
  [{:keys [timestamp justify? rtl? color]}]
  [react/text
   {:style (cond-> {:font-size  10
                    :align-self :flex-end
                    :color color}
             :justify-timestamp?
             (assoc :position              :absolute
                    :bottom                7
                    (if rtl? :left :right) 12))}
   timestamp])

(defview quoted-message
  [{:keys [from text]} outgoing current-public-key]
  (letsubs [username [:contacts/contact-name-by-identity from]]
    [react/view {:style (style/quoted-message-container outgoing)}
     [react/view {:style style/quoted-message-author-container}
      [vector-icons/icon :tiny-icons/tiny-reply {:color (if outgoing colors/wild-blue-yonder colors/gray)}]
      (chat.utils/format-reply-author from username current-public-key (partial style/quoted-message-author outgoing))]

     [react/text {:style           (style/quoted-message-text outgoing)
                  :number-of-lines 5}
      text]]))

(defn expand-button
  [expanded? chat-id message-id]
  [react/text {:style    style/message-expand-button
               :on-press #(re-frame/dispatch [:chat.ui/message-expand-toggled chat-id message-id])}
   (i18n/label (if expanded? :show-less :show-more))])

(defmulti message-content (fn [message] (message :content-type)))

(defmethod message-content constants/content-type-text
  [{:keys [chat-id message-id content timestamp-opts first-in-group?
           group-chat outgoing current-public-key expanded?] :as message}]
  (let [collapsible? (and (:should-collapse? content) group-chat)]
    [react/view
     (cond-> {:padding-vertical   6
              :padding-horizontal 12
              :border-radius      8
              :margin-top         (if (and first-in-group? (or outgoing (not group-chat))) 16 4)
              :background-color   (if outgoing colors/blue colors/blue-light)})
     (when (:response-to content)
       [quoted-message (:response-to content) outgoing current-public-key])
     (apply react/nested-text
            (cond-> {:style (style/text-message collapsible? outgoing)
                     :text-break-strategy :balanced}
              (and collapsible? (not expanded?))
              (assoc :number-of-lines constants/lines-collapse-threshold))
            (conj (if-let [render-recipe (:render-recipe content)]
                    (chat.utils/render-chunks render-recipe message)
                    [(:text content)])
                  [{:style (style/message-timestamp-placeholder outgoing)}
                   (str "  " (:timestamp timestamp-opts))]))
     (when collapsible?
       [expand-button expanded? chat-id message-id])
     [message-timestamp timestamp-opts]]))

(defmethod message-content constants/content-type-status
  [{:keys [content]}]
  [react/view style/status-container
   [react/text {:style {:margin-top  9
                        :font-size   14
                        :color       colors/gray}}
    (:text content)]])

#_(defmethod message-content constants/content-type-command
    [{:keys [first-ingroup? group-chat outgoing content] :as message}]
    (letsubs [id->command [:chats/id->command]
              {:keys [contacts]} [:chats/current-chat]]
      [react/view
       (cond-> {:padding-vertical   6
                :padding-horizontal 12
                :border-radius      8
                :margin-top         (if (and first-in-group? (or outgoing (not group-chat))) 16 4)
                :background-color (if outgoing colors/blue colors/blue-light)
                :padding-top    12
                :padding-bottom 10})
       (let [{:keys [type] :as command} (commands-receiving/lookup-command-by-ref message id->command)
             extension-id (get-in content [:params :extension-id])]
         (if (and platform/mobile? extension-id
                  (extensions/valid-uri? extension-id)
                  (or (not type) (and type (satisfies? protocol/Extension type)
                                      (not= extension-id (protocol/extension-id type)))))
           ;; Show install message only for mobile and if message contains extension id and there is no extension installed
           ;; or installed extension has differen extension id
           [install-extension-message extension-id outgoing]
           (if command
             (commands/generate-preview command (commands/add-chat-contacts contacts message))
             [react/text (str "Unhandled command: " (-> content :command-path first))])))]))

(defmethod message-content constants/content-type-emoji
  [{:keys [content first-in-group? outgoing group-chat
           content-type timestamp-opts justify-timestamp? outgoing] :as message}]
  [react/view
   (cond-> {:padding-vertical   6
            :padding-horizontal 12
            :border-radius      8
            :margin-top         (if (and first-in-group? (or outgoing (not group-chat))) 16 4)}
     (= content-type constants/content-type-emoji)
     (assoc :flex-direction :row)
     (not= content-type constants/content-type-emoji)
     (assoc :background-color (if outgoing colors/blue colors/blue-light)))
   [react/text {:style style/emoji-message}
    (:text content)]
   [message-timestamp timestamp-opts]])

(defmethod message-content constants/content-type-sticker
  [{:keys [content] :as message}]
  [react/image {:style {:margin 10 :width 140 :height 140}
                :source {:uri (:uri content)}}])

(defmethod message-content :default
  [{:keys [content-type first-in-group? outgoing group-chat timestamp-opts] :as message}]
  [react/view
   (cond-> {:padding-vertical   6
            :padding-horizontal 12
            :border-radius      8
            :background-color   (if outgoing colors/blue colors/blue-light)
            :margin-top         (if (and first-in-group? (or outgoing (not group-chat))) 16 4)})
   [react/text (str "Unhandled content-type " content-type)]
   [message-timestamp timestamp-opts]])

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
  (letsubs [current-network [:network-name]]
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

(defview message-author-name [from message-username]
  (letsubs [username [:contacts/contact-name-by-identity from]]
    (chat.utils/format-author from (or username message-username) style/message-author-name)))

(defn open-chat-context-menu
  [{:keys [message-id old-message-id content] :as message}]
  (list-selection/chat-message message-id old-message-id (:text content) (i18n/label :t/message)))

(defn chat-message
  [{:keys [outgoing last? typing group-chat modal? from last-in-group?
           current-public-key content-type content display-photo?
           display-username? username first-in-group?] :as message}]
  [react/touchable-highlight
   {:on-press      #() #_(fn [arg]
                           (if (and platform/desktop?
                                    (= "right" (.-button (.-nativeEvent arg))))
                             (open-chat-context-menu message)
                             (do
                               (when (= content-type constants/content-type-sticker)
                                 (re-frame/dispatch [:stickers/open-sticker-pack
                                                     (:pack content)]))
                               (re-frame/dispatch [:chat.ui/set-chat-ui-props
                                                   {:messages-focused? true
                                                    :show-stickers?    false}])
                               (when-not platform/desktop?
                                 (react/dismiss-keyboard!)))))
    :on-long-press #() #_#(when (#{constants/content-type-emoji
                                   constants/content-type-text}
                                 content-type)
                            (open-chat-context-menu message))}
   [react/view (merge {:accessibility-label :chat-item
                       :flex-direction :column}
                      (if outgoing
                        {:margin-left 64}
                        {:margin-right 64})
                      (when (and last? (not typing))
                        {:padding-bottom 16}))
    [react/view (cond-> {:padding-top (if (and display-username?
                                               first-in-group?)
                                        6
                                        2)}
                  outgoing       (assoc :flex-direction :row-reverse
                                        :align-self     :flex-end
                                        :align-items    :flex-end)
                  (not outgoing) (assoc :flex-direction :row
                                        :align-self     :flex-start
                                        :align-items    :flex-start)
                  display-photo?  (assoc :padding-left 8))
     #_(when display-photo?
         [react/view style/message-author
          (when last-in-group?
            [react/touchable-highlight
             {:on-press #(when-not modal?
                           (re-frame/dispatch [:chat.ui/show-profile from]))}
             [react/view
              [photos/member-photo from]]])])
     [react/view (cond-> {:flex-direction :column
                          :max-width      (if platform/desktop? 500 320)}
                   outgoing       (assoc :margin-right 8
                                         :align-items :flex-end)
                   (not outgoing) (assoc :margin-left 8
                                         :align-items :flex-start))
      #_(when display-username?
          [message-author-name from username])
      [react/view {:style {:flex-direction (if outgoing
                                             :row-reverse
                                             :row)}}
       [message-content message]]]]
    [react/view (style/delivery-status outgoing)
     [message-delivery-status message]]]])
