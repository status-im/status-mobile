(ns status-im.ui.screens.desktop.main.chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [clojure.string :as string]
            [status-im.ui.screens.chat.styles.message.message :as message.style]
            [status-im.ui.screens.chat.message.message :as message]
            [taoensso.timbre :as log]
            [reagent.core :as reagent]
            [status-im.chat.models :as models.chat]
            [status-im.group-chats.core :as models.group-chats]
            [status-im.ui.screens.chat.utils :as chat-utils]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as constants]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.datetime :as time]
            [status-im.utils.core :as core-utils]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.chat.message.datemark :as message.datemark]
            [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.desktop.main.chat.styles :as styles]
            [status-im.contact.db :as contact.db]
            [status-im.ui.screens.chat.views :as views.chat]
            [status-im.ui.components.popup-menu.views :refer [show-desktop-menu
                                                              get-chat-menu-items]]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.desktop.main.chat.events :as chat.events]
            [status-im.ui.screens.chat.message.message :as chat.message]))

(views/defview toolbar-chat-view [{:keys [chat-id color public-key public? group-chat]
                                   :as current-chat}]
  (views/letsubs [chat-name         [:chats/current-chat-name]
                  {:keys [pending? public-key photo-path]} [:chats/current-chat-contact]]
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
     [react/touchable-highlight
      {:on-press #(show-desktop-menu
                   (get-chat-menu-items group-chat public? chat-id))}
      [vector-icons/icon :icons/dots-horizontal
       {:style {:tint-color colors/black
                :width      24
                :height     24}}]]]))

(views/defview message-author-name [{:keys [from]}]
  (views/letsubs [incoming-name   [:contacts/contact-name-by-identity from]]
    [react/view {:flex-direction :row}
     (when incoming-name
       [react/text {:style styles/author} incoming-name])
     [react/text {:style styles/author-generated}
      (str (when incoming-name " • ") (gfycat/generate-gfy from))]]))

(views/defview member-photo [from]
  (views/letsubs [current-public-key [:account/public-key]
                  photo-path [:chats/photo-path from]]
    [react/view {:style {:width 40 :margin-horizontal 16}}
     [react/view {:style {:position :absolute}}
      [react/touchable-highlight {:on-press #(when-not (= current-public-key from)
                                               (re-frame/dispatch [:show-profile-desktop from]))}
       [react/view {:style styles/member-photo-container}
        [react/image {:source {:uri (if (string/blank? photo-path)
                                      (identicon/identicon from)
                                      photo-path)}
                      :style  styles/photo-style}]]]]]))

(views/defview quoted-message [{:keys [from text]} outgoing current-public-key]
  (views/letsubs [username [:contacts/contact-name-by-identity from]]
    [react/view {:style styles/quoted-message-container}
     [react/view {:style styles/quoted-message-author-container}
      [icons/icon :icons/reply {:style           (styles/reply-icon outgoing)
                                :width           16
                                :height          16
                                :container-style (when outgoing {:opacity 0.4})}]
      [react/text {:style (message.style/quoted-message-author outgoing)}
       (chat-utils/format-reply-author from username current-public-key)]]
     [react/text {:style           (message.style/quoted-message-text outgoing)
                  :number-of-lines 5}
      (core-utils/truncate-str text constants/chars-collapse-threshold)]]))

(defn- message-sent? [user-statuses current-public-key]
  (not= (get-in user-statuses [current-public-key :status]) :not-sent))

(views/defview message-without-timestamp
  [text {:keys [chat-id message-id old-message-id content group-chat expanded? current-public-key user-statuses] :as message} style]
  [react/view {:flex 1 :margin-vertical 5}
   [react/touchable-highlight {:on-press (fn [arg]
                                           (when (= "right" (.-button (.-nativeEvent arg)))
                                             (show-desktop-menu
                                              [{:text (i18n/label :t/sharing-copy-to-clipboard)
                                                :on-select #(do (utils/show-popup "" "Message copied to clipboard") (react/copy-to-clipboard text))}
                                               {:text (i18n/label :t/message-reply)
                                                :on-select #(when (message-sent? user-statuses current-public-key)
                                                              (re-frame/dispatch [:chat.ui/reply-to-message message-id old-message-id]))}])))}
    (let [collapsible? (and (:should-collapse? content) group-chat)
          char-limit (if (and collapsible? (not expanded?))
                       constants/chars-collapse-threshold constants/desktop-msg-chars-hard-limit)
          message-text (core-utils/truncate-str (:text content) char-limit)]
      [react/view {:style styles/message-container}
       (when (:response-to content)
         [quoted-message (:response-to content) false current-public-key])
       [react/text {:style           (styles/message-text collapsible? false)
                    :selectable      true
                    :selection-color colors/blue-light}
        (if-let [render-recipe (:render-recipe content)]
          (chat-utils/render-chunks-desktop char-limit render-recipe message-text)
          message-text)]
       (when collapsible?
         [message/expand-button expanded? chat-id message-id])])]])

(views/defview photo-placeholder []
  [react/view {:style {:width             40
                       :margin-horizontal 16}}])

(views/defview system-message [text {:keys [content from first-in-group? timestamp] :as message}]
  [react/view
   [react/view {:style {:flex-direction :row :margin-top 24}}
    [member-photo from]
    [react/view {:style {:flex 1}}]
    [react/text {:style styles/message-timestamp}
     (time/timestamp->time timestamp)]]
   [react/view {:style styles/not-first-in-group-wrapper}
    [photo-placeholder]
    [react/text {:style styles/system-message-text}
     text]]])

(views/defview message-with-name-and-avatar [text {:keys [from first-in-group? timestamp] :as message}]
  [react/view
   (when first-in-group?
     [react/view {:style {:flex-direction :row :margin-top 24}}
      [member-photo from]
      [message-author-name message]
      [react/view {:style {:flex 1}}]
      [react/text {:style styles/message-timestamp}
       (time/timestamp->time timestamp)]])
   [react/view {:style styles/not-first-in-group-wrapper}
    [photo-placeholder]
    [message-without-timestamp text message]]])

(defmulti message (fn [_ _ {:keys [content-type]}] content-type))

(defmethod message constants/content-type-command
  [_ _ {:keys [from] :as message}]
  [react/view
   [react/view {:style {:flex-direction :row :align-items :center :margin-top 15}}
    [member-photo from]
    [message-author-name message]]
   [react/view {:style styles/not-first-in-group-wrapper}
    [photo-placeholder]
    [react/view {:style styles/message-command-container}
     [message/message-content-command message]]]])

(views/defview message-content-status [text message]
  [react/view
   [system-message text message]])

(defmethod message constants/content-type-status
  [text _ message]
  [message-content-status text message])

(defmethod message :default
  [text me? {:keys [message-id chat-id message-status user-statuses from
                    current-public-key content-type outgoing type value] :as message}]
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
           ;;TODO(rasom): revisit this when seen messages will be reimplemented
           #_(re-frame/dispatch [:send-seen! {:chat-id    chat-id
                                              :from       from
                                              :message-id message-id}]))
        :reagent-render
        (fn []
          ^{:key (str "message" message-id)}
          [react/view
           [message-with-name-and-avatar text message]
           [react/view {:style (message.style/delivery-status outgoing)}
            [message/message-delivery-status message]]])}))))

(def load-step 5)

(defn load-more [all-messages-count messages-to-load]
  (let [next-count (min all-messages-count (+ @messages-to-load load-step))]
    (reset! messages-to-load next-count)))

(views/defview messages-view [{:keys [chat-id group-chat]}]
  (views/letsubs [messages [:chats/current-chat-messages-stream]
                  current-public-key [:account/public-key]
                  messages-to-load (reagent/atom load-step)
                  chat-id* (reagent/atom nil)]
    {:component-did-update #(if (:messages-initialized? (second (.-argv (.-props %1))))
                              (load-more (count messages) messages-to-load)
                              (re-frame/dispatch [:chat.ui/load-more-messages]))
     :component-did-mount  #(if (:messages-initialized? (second (.-argv (.-props %1))))
                              (load-more (count messages) messages-to-load)
                              (re-frame/dispatch [:chat.ui/load-more-messages]))}
    (let [scroll-ref (atom nil)
          scroll-timer (atom nil)
          scroll-height (atom nil)
          _ (when (or (not @chat-id*) (not= @chat-id* chat-id))
              (do
                (reset! messages-to-load load-step)
                (reset! chat-id* chat-id)))]
      [react/view {:style styles/messages-view}
       [react/scroll-view {:scrollEventThrottle    16
                           :headerHeight styles/messages-list-vertical-padding
                           :footerWidth styles/messages-list-vertical-padding
                           :enableArrayScrollingOptimization true
                           :inverted true
                           :on-scroll              (fn [e]
                                                     (let [ne (.-nativeEvent e)
                                                           y (.-y (.-contentOffset ne))]
                                                       (when (<= y 0)
                                                         (when @scroll-timer (js/clearTimeout @scroll-timer))
                                                         (reset! scroll-timer (js/setTimeout #(re-frame/dispatch [:chat.ui/load-more-messages]) 300)))
                                                       (reset! scroll-height (+ y (.-height (.-layoutMeasurement ne))))))
                           :ref                    #(reset! scroll-ref %)}
        [react/view
         (doall
          (for [{:keys [from content] :as message-obj} (take @messages-to-load messages)]
            ^{:key message-obj}
            [message (:text content) (= from current-public-key)
             (assoc message-obj :group-chat group-chat
                    :current-public-key current-public-key)]))]]
       [connectivity/connectivity-view]])))

(views/defview send-button [inp-ref disconnected?]
  (views/letsubs [{:keys [input-text]} [:chats/current-chat]]
    (let [empty? (= "" input-text)
          inactive? (or empty? disconnected?)]
      [react/touchable-highlight {:style    styles/send-button
                                  :disabled inactive?
                                  :on-press (fn []
                                              (when-not inactive?
                                                (.clear @inp-ref)
                                                (.focus @inp-ref)
                                                (re-frame/dispatch [:chat.ui/send-current-message])))}
       [react/view {:style (styles/send-icon inactive?)}
        [icons/icon :icons/arrow-left {:style (styles/send-icon-arrow inactive?)}]]])))

(views/defview reply-message [from message-text]
  (views/letsubs [username           [:contacts/contact-name-by-identity from]
                  current-public-key [:account/public-key]]
    [react/view {:style styles/reply-content-container}
     [react/text {:style styles/reply-content-author}
      (chat-utils/format-reply-author from username current-public-key)]
     [react/text {:style styles/reply-content-message} message-text]]))

(views/defview reply-member-photo [from]
  (views/letsubs [photo-path [:chats/photo-path from]]
    [react/image {:source {:uri (if (string/blank? photo-path)
                                  (identicon/identicon from)
                                  photo-path)}
                  :style  styles/reply-photo-style}]))

(views/defview reply-message-view []
  (views/letsubs [{:keys [content from] :as message} [:chats/reply-message]]
    (when message
      [react/view {:style styles/reply-wrapper}
       [react/view {:style styles/reply-container}
        [reply-member-photo from]
        [reply-message from (:text content)]]
       [react/touchable-highlight
        {:style               styles/reply-close-highlight
         :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-reply])
         :accessibility-label :cancel-message-reply}
        [react/view {}
         [icons/icon :icons/close {:style styles/reply-close-icon}]]]])))

(views/defview chat-text-input [chat-id input-text]
  (views/letsubs [inp-ref (atom nil)
                  disconnected? [:disconnected?]]
    {:component-will-update
     (fn [e [_ new-chat-id new-input-text]]
       (let [[_ old-chat-id] (.. e -props -argv)]
         (when (not= old-chat-id new-chat-id)
           ;; reset default text when switch to another chat
           (.setNativeProps @inp-ref #js {:text (or new-input-text "")}))))}
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
                          :on-submit-editing      #(when-not disconnected?
                                                     (.clear @inp-ref)
                                                     (.focus @inp-ref)
                                                     (re-frame/dispatch [:chat.ui/send-current-message]))
                          :on-change              (fn [e]
                                                    (let [native-event (.-nativeEvent e)
                                                          text         (.-text native-event)]
                                                      (re-frame/dispatch [:chat.ui/set-chat-input-text text])))}]
       [send-button inp-ref disconnected?]])))

(defn not-joined-group-chat? [chat current-public-key]
  (and (models.chat/group-chat? chat)
       (models.group-chats/invited? current-public-key chat)
       (not (models.group-chats/joined? current-public-key chat))))

(views/defview chat-view []
  (views/letsubs [{:keys [input-text chat-id] :as current-chat} [:chats/current-chat]
                  current-public-key [:account/public-key]]

    [react/view {:style styles/chat-view}
     [toolbar-chat-view current-chat]
     [react/view {:style styles/separator}]
     (if (not-joined-group-chat? current-chat current-public-key)
       [views.chat/group-chat-join-section current-public-key current-chat]
       [messages-view current-chat])
     [react/view {:style styles/separator}]
     [reply-message-view]
     [chat-text-input chat-id input-text]]))

(views/defview chat-profile []
  (views/letsubs [identity        [:contacts/current-contact-identity]
                  maybe-contact   [:contacts/current-contact]]
    (let [contact (or maybe-contact (contact.db/public-key->new-contact identity))
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
