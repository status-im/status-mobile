(ns status-im.ui.screens.desktop.main.chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [clojure.string :as string]
            [status-im.ui.screens.chat.styles.message.message :as message.style]
            [status-im.ui.screens.chat.message.message :as message]
            [taoensso.timbre :as log]
            [status-im.ui.components.list.views :as list]
            [reagent.core :as reagent]
            [status-im.ui.screens.chat.utils :as chat-utils]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as constants]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.datetime :as time]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.message.datemark :as message.datemark]
            [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.desktop.main.chat.styles :as styles]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.desktop.main.chat.events :as chat.events]
            [status-im.ui.screens.chat.message.message :as chat.message]))

(defn toolbar-chat-view [{:keys [chat-id color public? group-chat chat-name contact]
                          :as current-chat}]
  (let [{:keys [photo-path public-key pending?]} contact]
    [react/view {:style styles/toolbar-chat-view}
     [react/view {:style {:flex-direction :row
                          :flex 1}}
      (if public?
        [react/view {:style (styles/topic-image color)}
         [react/text {:style styles/topic-text}
          (string/capitalize (second chat-name))]]
        [react/image {:style styles/chat-icon
                      :source {:uri photo-path}}])
      [react/view {:style (styles/chat-title-and-type pending?)}
       [react/text {:style styles/chat-title
                    :font  :medium}
        chat-name]
       (cond pending?
             [react/text {:style styles/add-contact-text
                          :on-press #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])}
              (i18n/label :t/add-to-contacts)]
             public?
             [react/text {:style styles/public-chat-text}
              (i18n/label :t/public-chat)])]]
     [react/view
      (when (and (not group-chat) (not public?))
        [react/text {:style (styles/profile-actions-text colors/black)
                     :on-press #(re-frame/dispatch [:show-profile-desktop public-key])}
         (i18n/label :t/view-profile)])
      (when (and group-chat (not public?))
        [react/text {:style (styles/profile-actions-text colors/black)
                     :on-press #(re-frame/dispatch [:show-group-chat-profile])}
         (i18n/label :t/group-info)])
      [react/text {:style (styles/profile-actions-text colors/black)
                   :on-press #(re-frame/dispatch [:chat.ui/clear-history-pressed])}
       (i18n/label :t/clear-history)]
      [react/text {:style (styles/profile-actions-text colors/black)
                   :on-press #(re-frame/dispatch [(if (and group-chat (not public?))
                                                    :group-chats.ui/remove-chat-pressed
                                                    :chat.ui/remove-chat-pressed)
                                                  chat-id])}
       (i18n/label :t/delete-chat)]]]))

(defn message-author-name [style {:keys [from user-name generated-name]}]
  [react/view {:flex-direction :row}
   (when user-name
     [react/text {:style style} user-name])
   (when (and user-name generated-name)
     [react/text {:style style} " :: "])
   (when generated-name
     [react/text {:style style} generated-name])])

(defn member-photo [from photo-path on-press-photo-fn]
  [react/view {:style {:width 40 :margin-horizontal 16}}
   [react/view {:style {:position :absolute}}
    [react/touchable-highlight {:on-press on-press-photo-fn}
     [react/view {:style styles/member-photo-container}
      [react/image {:source {:uri photo-path}
                    :style  styles/photo-style}]]]]])

(defn quoted-message
  [{:keys [from text user-name generated-name on-press-photo-fn] :as message} outgoing]
  [react/view {:style styles/quoted-message-container}
   [react/view {:style styles/quoted-message-author-container}
    [icons/icon :icons/reply {:style           (styles/reply-icon outgoing)
                              :width           16
                              :height          16
                              :container-style (when outgoing {:opacity 0.4})}]
    [message-author-name (message.style/quoted-message-author outgoing) message]]
   [react/text {:style           (message.style/quoted-message-text outgoing)
                :number-of-lines 5}
    text]])

(defn message-without-timestamp
  [{:keys [message-id content user-statuses can-reply?] :as message} style]
  [react/view {:flex 1 :margin-vertical 5}
   [react/touchable-highlight {:on-press #(if (= "right" (.-button (.-nativeEvent %)))
                                            (do (utils/show-popup "" "Message copied to clipboard")
                                                (react/copy-to-clipboard (:text content)))
                                            (when can-reply?
                                              (re-frame/dispatch [:chat.ui/reply-to-message message-id])))}
    [react/view {:style styles/message-container}
     (when (:response-to content)
       [quoted-message (:response-to content) false])
     [react/text {:style           (styles/message-text false)
                  :selectable      true
                  :selection-color colors/blue-light}
      (if-let [render-recipe (:render-recipe content)]
        (chat-utils/render-chunks render-recipe message)
        (:text content))]]]])

(defn photo-placeholder []
  [react/view {:style {:width             40
                       :margin-horizontal 16}}])

(defn message-with-name-and-avatar
  [{:keys [from first-in-group? timestamp photo-path on-press-photo-fn] :as message}]
  [react/view
   (when first-in-group?
     [react/view {:style {:flex-direction :row :margin-top 24}}
      [member-photo from photo-path on-press-photo-fn]
      [message-author-name styles/author message]
      [react/view {:style {:flex 1}}]
      [react/text {:style styles/message-timestamp}
       (time/timestamp->time timestamp)]])
   [react/view {:style styles/not-first-in-group-wrapper}
    [photo-placeholder]
    [message-without-timestamp message]]])

(defmulti message-view (fn [{:keys [content-type]}] content-type))

(defmethod message-view
  constants/content-type-command
  [{:keys [from photo-path content] :as message}]
  [react/view
   [react/view {:style {:flex-direction :row :align-items :center :margin-top 15}}
    [member-photo from photo-path]
    [message-author-name message]]
   [react/view {:style styles/not-first-in-group-wrapper}
    [photo-placeholder]
    [react/view {:style styles/message-command-container}
     [message/message-content-command message]]]])

(defmethod message-view
  constants/content-type-status
  [{:keys [content from first-in-group? timestamp photo-path on-press-photo-fn] :as message}]
  [react/view
   [react/view {:style {:flex-direction :row :margin-top 24}}
    [member-photo from photo-path on-press-photo-fn]
    [react/view {:style {:flex 1}}]
    [react/text {:style styles/message-timestamp}
     (time/timestamp->time timestamp)]]
   [react/view {:style styles/not-first-in-group-wrapper}
    [photo-placeholder]
    [react/text {:style styles/system-message-text} (:text content)]]])

(defmethod message-view
  :default
  [{:keys [content message-id chat-id message-status user-statuses from on-seen-message-fn
           content-type outgoing type value] :as message}]
  (if (= type :datemark)
    ^{:key (str "datemark" message-id)}
    [message.datemark/chat-datemark value]
    (when (contains? constants/desktop-content-types content-type)
      (when (nil? message-id)
        (log/debug "nil?" message))
      (reagent.core/create-class
       {:component-did-mount on-seen-message-fn
        :reagent-render
        (fn []
          ^{:key (str "message" message-id)}
          [react/view
           [message-with-name-and-avatar message]
           [react/view {:style (message.style/delivery-status outgoing)}
            [message/message-delivery-status message]]])}))))

(defn messages-view [{:keys [messages all-loaded? group-chat] :as current-chat}]
  [react/view {:style styles/messages-view}
   [list/flat-list {:data                messages
                    :initialNumToRender  20
                    :headerHeight        styles/messages-list-vertical-padding
                    :footerWidth         styles/messages-list-vertical-padding
                    :key-fn              #(or (:message-id %) (:value %))
                    :render-fn           (fn [message]
                                           [message-view (assoc message :group-chat group-chat)])
                    :inverted            true
                    :onEndReached        #(re-frame/dispatch [:chat.ui/load-more-messages])
                    :enableEmptySections true}]
   [connectivity/error-view]])

(defn send-button [input-text inp-ref network-status]
  (let [empty? (= "" input-text)
        offline? (= :offline network-status)
        inactive? (or empty? offline?)]
    [react/touchable-highlight {:style    styles/send-button
                                :disabled inactive?
                                :on-press (fn []
                                            (when-not inactive?
                                              (.clear @inp-ref)
                                              (.focus @inp-ref)
                                              (re-frame/dispatch [:chat.ui/send-current-message])))}
     [react/view {:style (styles/send-icon inactive?)}
      [icons/icon :icons/arrow-left {:style (styles/send-icon-arrow inactive?)}]]]))

(defn reply-message [{:keys [content] :as message}]
  [react/view {:style styles/reply-content-container}
   [message-author-name styles/reply-content-author message]
   [react/text {:style styles/reply-content-message} (:text content)]])

(defn reply-message-view [{:keys [photo-path] :as message}]
  (when message
    [react/view {:style styles/reply-wrapper}
     [react/view {:style styles/reply-container}
      [react/image {:source {:uri photo-path}
                    :style  styles/reply-photo-style}]
      [reply-message message]]
     [react/touchable-highlight
      {:style               styles/reply-close-highlight
       :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
       :accessibility-label :cancel-message-reply}
      [react/view {}
       [icons/icon :icons/close {:style styles/reply-close-icon}]]]]))

(defn chat-text-input [chat-id input-text network-status]
  (let [inp-ref (atom nil)]
    (reagent/create-class
     {:component-will-update
      (fn [e [_ new-chat-id new-input-text]]
        (let [[_ old-chat-id] (.. e -props -argv)]
          (when (not= old-chat-id new-chat-id)
            ;; reset default text when switch to another chat
            (.setNativeProps @inp-ref #js {:text (or new-input-text "")}))))
      :reagent-render (fn [chat-id input-text]
                        (let [component               (reagent/current-component)
                              set-container-height-fn #(reagent/set-state component {:container-height %})
                              {:keys [container-height]} (reagent/state component)]
                          [react/view {:style (styles/chat-box container-height)}
                           [react/text-input {:placeholder            (i18n/label :t/type-a-message)
                                              :auto-focus             true
                                              :multiline              true
                                              :blur-on-submit         true
                                              :style                  (styles/chat-text-input container-height)
                                              :font                   :default
                                              :ref                    #(reset! inp-ref %)
                                              :default-value          input-text
                                              :on-content-size-change #(set-container-height-fn (.-height (.-contentSize (.-nativeEvent %))))
                                              :submit-shortcut        {:key "Enter"}
                                              :on-submit-editing      #(when (= :online network-status)
                                                                         (.clear @inp-ref)
                                                                         (.focus @inp-ref)
                                                                         (re-frame/dispatch [:chat.ui/send-current-message]))
                                              :on-change              (fn [e]
                                                                        (let [native-event (.-nativeEvent e)
                                                                              text         (.-text native-event)]
                                                                          (re-frame/dispatch [:chat.ui/set-chat-input-text text])))}]
                           [send-button input-text inp-ref network-status]]))})))

(views/defview chat-view []
  (views/letsubs [{:keys [input-text chat-id group-chat] :as current-chat} [:chat/current]
                  reply-message [:get-reply-message]
                  network-status [:network-status]]
    [react/view {:style styles/chat-view}
     [toolbar-chat-view current-chat]
     [react/view {:style styles/separator}]
     [messages-view current-chat]
     [react/view {:style styles/separator}]
     [reply-message-view reply-message]
     [chat-text-input chat-id input-text network-status]]))

(views/defview chat-profile []
  (views/letsubs [identity        [:get-current-contact-identity]
                  maybe-contact   [:get-current-contact]]
    (let [contact (or maybe-contact (utils.contacts/public-key->new-contact identity))
          {:keys [pending? public-key]} contact]
      [react/view {:style styles/chat-profile-body}
       [profile.views/profile-badge contact]
       ;; for private chat, public key will be chat-id
       [react/view
        (if (or (nil? pending?) pending?)
          [react/touchable-highlight {:on-press #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])}
           [react/view {:style styles/chat-profile-row}
            [react/view {:style styles/chat-profile-icon-container
                         :accessibility-label :add-contact-link}
             [vector-icons/icon :icons/add {:style (styles/chat-profile-icon colors/blue)}]]
            [react/text {:style (styles/contact-card-text colors/blue)} (i18n/label :t/add-to-contacts)]]]
          [react/view {:style styles/chat-profile-row}
           [react/view {:style styles/chat-profile-icon-container
                        :accessibility-label :add-contact-link}
            [vector-icons/icon :icons/add {:style (styles/chat-profile-icon colors/gray)}]]
           [react/text {:style (styles/contact-card-text colors/gray)} (i18n/label :t/in-contacts)]])
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch
                      [:contact.ui/send-message-pressed {:public-key public-key}])}
         [react/view {:style styles/chat-profile-row}
          [react/view {:style styles/chat-profile-icon-container
                       :accessibility-label :send-message-link}
           [vector-icons/icon :icons/chats {:style (styles/chat-profile-icon colors/blue)}]]
          [react/text {:style (styles/contact-card-text colors/blue)}
           (i18n/label :t/send-message)]]]
        [react/text {:style styles/chat-profile-contact-code} (i18n/label :t/contact-code)]
        [react/text {:style           {:font-size 14}
                     :selectable      true
                     :selection-color colors/blue} public-key]]])))
