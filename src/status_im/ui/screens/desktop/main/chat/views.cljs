(ns status-im.ui.screens.desktop.main.chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [clojure.string :as string]
            [status-im.chat.styles.message.message :as message.style]
            [status-im.utils.gfycat.core :as gfycat.core]
            [taoensso.timbre :as log]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as constants]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.datetime :as time]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.chat.views.message.datemark :as message.datemark]
            [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.desktop.main.chat.styles :as styles]
            [status-im.utils.contacts :as utils.contacts]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.desktop.main.chat.events :as chat.events]))

(views/defview toolbar-chat-view [{:keys [chat-id color public-key public? group-chat]
                                   :as current-chat}]
  (views/letsubs [chat-name         [:get-current-chat-name]
                  {:keys [pending? whisper-identity photo-path]} [:get-current-chat-contact]]
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
                          :on-press #(re-frame/dispatch [:add-contact whisper-identity])}
              (i18n/label :t/add-to-contacts)]
             public?
             [react/text {:style styles/public-chat-text}
              (i18n/label :t/public-chat)])]]
     [react/view
      (when (and (not group-chat) (not public?))
        [react/text {:style (styles/profile-actions-text colors/black)
                     :on-press #(re-frame/dispatch [:show-profile-desktop whisper-identity])}
         (i18n/label :t/view-profile)])

      [react/text {:style (styles/profile-actions-text colors/red)
                   :on-press (fn []
                               (utils/show-confirmation (i18n/label :clear-history-confirmation)
                                                        ""
                                                        (i18n/label :clear-history-action)
                                                        #(re-frame/dispatch [:clear-history])))}
       (i18n/label :t/clear-history)]
      [react/text {:style (styles/profile-actions-text colors/red)
                   :on-press (fn []
                               (utils/show-confirmation (i18n/label :delete-chat-confirmation)
                                                        ""
                                                        (i18n/label :delete-chat-action)
                                                        #(re-frame/dispatch [:remove-chat-and-navigate-home chat-id])))}
       (i18n/label :t/delete-chat)]]]))

(views/defview message-author-name [{:keys [outgoing from] :as message}]
  (views/letsubs [current-account [:get-current-account]
                  incoming-name   [:get-contact-name-by-identity from]]
    (let [name (or incoming-name (gfycat/generate-gfy from))]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-contact-dialog from name (boolean incoming-name)])}
       [react/text {:style styles/author} name]])))

(views/defview member-photo [from]
  (letsubs [photo-path [:get-photo-path from]]
    [react/touchable-highlight  {:on-press #(re-frame/dispatch [:show-profile-desktop from])}
     [react/image {:source {:uri (if (string/blank? photo-path)
                                   (identicon/identicon from)
                                   photo-path)}
                   :style  styles/photo-style}]]))

(views/defview my-photo [from]
  (views/letsubs [account [:get-current-account]]
    (let [{:keys [photo-path]} account]
      [react/view
       [react/image {:source {:uri (if (string/blank? photo-path)
                                     (identicon/identicon from)
                                     photo-path)}
                     :style  styles/photo-style}]])))

(views/defview message-with-timestamp [text {:keys [timestamp outgoing] :as message} style]
  [react/view {:style style}
   [react/view {:style styles/message-wrapper}
    [react/text {:style           (styles/message-text message)
                 :selectable      true
                 :selection-color (if outgoing colors/white colors/hawkes-blue)}
     text]
    [react/text {:style (styles/message-timestamp-placeholder)}
     (time/timestamp->time timestamp)]
    [react/text {:style (styles/message-timestamp)}
     (time/timestamp->time timestamp)]]])

(views/defview text-only-message [text message]
  [react/view {:style (styles/message-row message)}
   [message-with-timestamp text message (styles/message-box message)]])

(views/defview photo-placeholder []
  [react/view {:style styles/photo-style}])

(views/defview message-with-name-and-avatar [text {:keys [from first-in-group? last-in-group?] :as message}]
  [react/view {:style (styles/message-row message)}
   [react/view {:style styles/message-row-column}
    (when first-in-group?
      [message-author-name message])
    [react/view {:style styles/not-first-in-group-wrapper}
     (if last-in-group?
       [member-photo from]
       [photo-placeholder])
     [message-with-timestamp text message (styles/message-box message)]]]])

(defn message [text me? {:keys [message-id chat-id message-status user-statuses from
                                current-public-key content-type group-chat outgoing type value] :as message}]
  (when (nil? message-id)
    (log/debug "nil?" message))
  (if (= type :datemark)
    ^{:key (str "datemark" message-id)}
    [message.datemark/chat-datemark value]
    (when (contains? constants/desktop-content-types content-type)
      (reagent.core/create-class
       {:component-did-mount
        #(when (and message-id
                    chat-id
                    (not outgoing)
                    (not= :seen message-status)
                    (not= :seen (keyword (get-in user-statuses [current-public-key :status]))))
           (re-frame/dispatch [:send-seen! {:chat-id    chat-id
                                            :from       from
                                            :message-id message-id}]))
        :reagent-render
        (fn []
          ^{:key (str "message" message-id)}
          (if (and group-chat (not outgoing))
            [message-with-name-and-avatar text message]
            [text-only-message text message]))}))))

(views/defview messages-view [{:keys [chat-id group-chat]}]
  (views/letsubs [chat-id* (atom nil)
                  scroll-ref (atom nil)
                  scroll-timer (atom nil)
                  scroll-height (atom nil)]
    (let [_ (when (or (not @chat-id*) (not= @chat-id* chat-id))
              (reset! chat-id* chat-id)
              (js/setTimeout #(when scroll-ref (.scrollToEnd @scroll-ref)) 400))
          messages (re-frame/subscribe [:get-current-chat-messages-stream])
          current-public-key (re-frame/subscribe [:get-current-public-key])]
      [react/view {:style styles/messages-view}
       [react/scroll-view {:scrollEventThrottle    16
                           :headerHeight styles/messages-list-vertical-padding
                           :footerWidth styles/messages-list-vertical-padding
                           :enableArrayScrollingOptimization true
                           :on-scroll              (fn [e]
                                                     (let [ne (.-nativeEvent e)
                                                           y (.-y (.-contentOffset ne))]
                                                       (when (<= y 0)
                                                         (when @scroll-timer (js/clearTimeout @scroll-timer))
                                                         (reset! scroll-timer (js/setTimeout #(re-frame/dispatch [:load-more-messages]) 300)))
                                                       (reset! scroll-height (+ y (.-height (.-layoutMeasurement ne))))))
                           :on-content-size-change #(.scrollToEnd @scroll-ref)
                           :ref                    #(reset! scroll-ref %)}
        [react/view
         (doall
          (for [[index {:keys [from content message-id type value] :as message-obj}] (map-indexed vector (reverse @messages))]
            ^{:key (or message-id (str type value))}
            [message content (= from @current-public-key) (assoc message-obj :group-chat group-chat)]))]]])))

(views/defview chat-text-input []
  (views/letsubs [inp-ref (atom nil)]
    [react/view {:style styles/chat-box}
     [react/view {:style styles/chat-box-inner}
      [react/view {:style {:flex 1}}
       [react/text-input {:placeholder    (i18n/label :t/type-a-message)
                          :auto-focus     true
                          :multiline      true
                          :blur-on-submit true
                          :style          styles/chat-text-input
                          :font           :default
                          :ref            #(reset! inp-ref %)
                          :on-key-press   (fn [e]
                                            (let [native-event (.-nativeEvent e)
                                                  key (.-key native-event)
                                                  modifiers (js->clj (.-modifiers native-event))
                                                  should-send (and (= key "Enter") (not (contains? (set modifiers) "shift")))]
                                              (when should-send
                                                (.clear @inp-ref)
                                                (.focus @inp-ref)
                                                (re-frame/dispatch [:send-current-message]))))
                          :on-change      (fn [e]
                                            (let [native-event (.-nativeEvent e)
                                                  text (.-text native-event)]
                                              (re-frame/dispatch [:set-chat-input-text text])))}]]
      [react/touchable-highlight {:on-press (fn []
                                              (.clear @inp-ref)
                                              (.focus @inp-ref)
                                              (re-frame/dispatch [:send-current-message]))}
       [react/view {:style styles/send-icon}
        [icons/icon :icons/arrow-left]]]]]))

(views/defview chat-view []
  (views/letsubs [current-chat [:get-current-chat]]
    [react/view {:style styles/chat-view}
     [toolbar-chat-view current-chat]
     [messages-view current-chat]
     [chat-text-input]]))

(views/defview chat-profile []
  (letsubs [identity        [:get-current-contact-identity]
            maybe-contact   [:get-current-contact]]

    (let [contact (or maybe-contact (utils.contacts/whisper-id->new-contact identity))
          {:keys [pending? whisper-identity public-key]} contact]
      [react/view {:style styles/chat-profile-body}
       [profile.views/profile-badge contact]
                  ;; for private chat, public key will be chat-id
       [react/view
        (if (or (nil? pending?) pending?)
          [react/touchable-highlight {:on-press #(re-frame/dispatch [:add-contact whisper-identity])}
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
        [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat public-key])}
         [react/view {:style styles/chat-profile-row}
          [react/view {:style styles/chat-profile-icon-container
                       :accessibility-label :send-message-link}
           [vector-icons/icon :icons/chats {:style (styles/chat-profile-icon colors/blue)}]]
          [react/text {:style (styles/contact-card-text colors/blue)}
           (i18n/label :t/send-message)]]]
        [react/text {:style styles/chat-profile-contact-code} (i18n/label :t/contact-code)]
        [react/text {:style           {:font-size 14}
                     :selectable      true
                     :selection-color colors/hawkes-blue} whisper-identity]]])))
