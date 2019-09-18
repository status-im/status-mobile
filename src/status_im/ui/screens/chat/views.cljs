(ns status-im.ui.screens.chat.views
  (:require [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.sheets :as sheets]
            [status-im.ui.screens.chat.input.input :as input]
            [status-im.ui.screens.chat.message.message :as message]
            [status-im.ui.screens.chat.stickers.views :as stickers]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.chat.toolbar-content :as toolbar-content]
            [status-im.ui.screens.chat.image.views :as image]
            [status-im.ui.screens.chat.state :as state]
            [status-im.utils.debounce :as debounce]
            [status-im.ui.screens.chat.extensions.views :as extensions]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.chat.group :as chat.group]
            [status-im.ui.screens.chat.message.gap :as gap]
            [status-im.ui.screens.chat.message.datemark :as message-datemark])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn topbar [current-chat]
  [topbar/topbar
   {:content      [toolbar-content/toolbar-content-view]
    :show-border? true
    :navigation   {:icon                :main-icons/back
                   :accessibility-label :back-button
                   :handler
                   #(re-frame/dispatch [:navigate-to :home])}
    :accessories  [{:icon                :main-icons/more
                    :accessibility-label :chat-menu-button
                    :handler
                    #(re-frame/dispatch [:bottom-sheet/show-sheet
                                         {:content (fn []
                                                     [sheets/actions current-chat])
                                          :height  256}])}]}])

(defn add-contact-bar
  [public-key]
  [react/touchable-highlight
   {:on-press
    #(re-frame/dispatch [:contact.ui/add-to-contact-pressed public-key])
    :accessibility-label :add-to-contacts-button}
   [react/view {:style (style/add-contact)}
    [vector-icons/icon :main-icons/add
     {:color colors/blue}]
    [react/i18n-text {:style style/add-contact-text :key :add-to-contacts}]]])

(defn intro-header
  [contact]
  [react/text {:style (assoc style/intro-header-description
                             :margin-bottom 32)}
   (str
    (i18n/label :t/empty-chat-description-one-to-one)
    (multiaccounts/displayed-name contact))])

(defn chat-intro-header-container
  [{:keys [group-chat name pending-invite-inviter-name color chat-id chat-name
           public? contact intro-status] :as chat}
   no-messages]
  (let [icon-text  (if public? chat-id name)
        intro-name (if public? chat-name (multiaccounts/displayed-name contact))]
    (when (or pending-invite-inviter-name
              (not= (get-in contact [:tribute-to-talk :snt-amount]) 0))
      [react/touchable-without-feedback
       {:style    {:flex        1
                   :align-items :flex-start}
        :on-press (fn [_]
                    (re-frame/dispatch
                     [:chat.ui/set-chat-ui-props {:input-bottom-sheet nil}])
                    (react/dismiss-keyboard!))}
       [react/view (style/intro-header-container intro-status no-messages)
        ;; Icon section
        [react/view {:style {:margin-top    42
                             :margin-bottom 24}}
         [chat-icon.screen/chat-intro-icon-view
          icon-text chat-id
          {:default-chat-icon      (style/intro-header-icon 120 color)
           :default-chat-icon-text style/intro-header-icon-text
           :size                   120}]]
        ;; Chat title section
        [react/text {:style (style/intro-header-chat-name)}
         (if group-chat chat-name intro-name)]
        ;; Description section
        (if group-chat
          [chat.group/group-chat-description-container chat]
          [intro-header contact])]])))

(defonce messages-list-ref (atom nil))

(defn on-viewable-items-changed [^js e]
  (when @messages-list-ref
    (reset! state/first-not-visible-item
            (when-let [^js last-visible-element (aget (.-viewableItems e) (dec (.-length ^js (.-viewableItems e))))]
              (let [index (.-index last-visible-element)
                    ;; Get first not visible element, if it's a datemark/gap
                    ;; we might unnecessarely add messages on receiving as
                    ;; they do not have a clock value, but most of the times
                    ;; it will be a message
                    first-not-visible (aget (.-data ^js (.-props ^js @messages-list-ref)) (inc index))]
                (when (and first-not-visible
                           (= :message (:type first-not-visible)))
                  first-not-visible)))))
  (debounce/debounce-and-dispatch [:chat.ui/message-visibility-changed e] 5000))

(defview messages-view
  [{:keys [group-chat chat-id pending-invite-inviter-name] :as chat}]
  (letsubs [messages           [:chats/current-chat-messages-stream]
            current-public-key [:multiaccount/public-key]]
    [list/flat-list
     {:key-fn                       #(or (:message-id %) (:value %))
      :ref                          #(reset! messages-list-ref %)
      :header                       (when pending-invite-inviter-name
                                      [chat.group/group-chat-footer chat-id])
      :footer                       [chat-intro-header-container chat (empty? messages)]
      :data                         messages
      :inverted                     true
      :render-fn                    (fn [{:keys [outgoing type] :as message} idx]
                                      (if (= type :datemark)
                                        [message-datemark/chat-datemark (:value message)]
                                        (if (= type :gap)
                                          [gap/gap message idx messages-list-ref]
                                          ; message content
                                          [message/chat-message
                                           (assoc message
                                                  :incoming-group (and group-chat (not outgoing))
                                                  :group-chat group-chat
                                                  :current-public-key current-public-key)])))
      :on-viewable-items-changed    on-viewable-items-changed
      :on-end-reached               #(re-frame/dispatch [:chat.ui/load-more-messages])
      :on-scroll-to-index-failed    #() ;;don't remove this
      :keyboard-should-persist-taps :handled}]))

(defview empty-bottom-sheet []
  (letsubs [input-bottom-sheet [:chats/empty-chat-panel-height]]
    [react/view {:height input-bottom-sheet}]))

(defview bottom-sheet []
  (letsubs [input-bottom-sheet [:chats/current-chat-ui-prop :input-bottom-sheet]]
    (case input-bottom-sheet
      :stickers
      [stickers/stickers-view]
      :extensions
      [extensions/extensions-view]
      :images
      [image/image-view]
      [empty-bottom-sheet])))

(defview chat []
  (letsubs [{:keys [chat-id show-input? group-chat contact] :as current-chat}
            [:chats/current-chat]]
    [react/view {:style {:flex 1}}
     [connectivity/connectivity
      [topbar current-chat]
      [react/view {:style {:flex 1}}
       ;;TODO contact.db/added? looks weird here, move to events
       (when (and (not group-chat) (not (contact.db/added? contact)))
         [add-contact-bar chat-id])
       [messages-view current-chat]]]
     (when show-input?
       [input/container])
     [bottom-sheet]]))
