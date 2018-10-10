(ns status-im.ui.screens.desktop.main.chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [clojure.string :as string]
            [status-im.ui.screens.chat.styles.message.message :as message.style]
            [status-im.ui.screens.chat.message.message :as message]
            [taoensso.timbre :as log]
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
            [status-im.ui.screens.chat.message.message :as chat.message]
            [status-im.utils.http :as http]))

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
                          :on-press #(re-frame/dispatch [:contact.ui/add-to-contact-pressed whisper-identity])}
              (i18n/label :t/add-to-contacts)]
             public?
             [react/text {:style styles/public-chat-text}
              (i18n/label :t/public-chat)])]]
     #_[react/view
        [react/popup-menu
         [react/popup-menu-trigger {:text "Popup test"}]
         [react/popup-menu-options
          [react/popup-menu-option {:text "First"}]
          [react/popup-menu-option {:text "Second"}]]]]
     [react/view
      (when (and (not group-chat) (not public?))
        [react/text {:style (styles/profile-actions-text colors/black)
                     :on-press #(re-frame/dispatch [:show-profile-desktop whisper-identity])}
         (i18n/label :t/view-profile)])
      (when (and group-chat (not public?))
        [react/text {:style (styles/profile-actions-text colors/black)
                     :on-press #(re-frame/dispatch [:show-group-chat-profile])}
         (i18n/label :t/group-info)])
      [react/text {:style (styles/profile-actions-text colors/black)
                   :on-press #(re-frame/dispatch [:chat.ui/clear-history-pressed])}
       (i18n/label :t/clear-history)]
      [react/text {:style (styles/profile-actions-text colors/black)
                   :on-press #(re-frame/dispatch [:chat.ui/remove-chat-pressed chat-id])}
       (i18n/label :t/delete-chat)]]]))

(views/defview message-author-name [{:keys [from]}]
  (views/letsubs [incoming-name   [:get-contact-name-by-identity from]]
    (let [name (chat-utils/format-author from incoming-name)]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-contact-dialog from name (boolean incoming-name)])}
       [react/text {:style styles/author :font :medium} name]])))

(views/defview member-photo [from]
  (views/letsubs [photo-path [:get-photo-path from]]
    [react/view {:style {:width 40 :margin-horizontal 16}}
     [react/view {:style {:position :absolute}}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-profile-desktop from])}
       [react/view {:style styles/member-photo-container}
        [react/image {:source {:uri (if (string/blank? photo-path)
                                      (identicon/identicon from)
                                      photo-path)}
                      :style  styles/photo-style}]]]]]))

(views/defview my-photo [from]
  (views/letsubs [account [:get-current-account]]
    (let [{:keys [photo-path]} account]
      [react/view
       [react/image {:source {:uri (if (string/blank? photo-path)
                                     (identicon/identicon from)
                                     photo-path)}
                     :style  styles/photo-style}]])))

(views/defview quoted-message [{:keys [from text]} outgoing current-public-key]
  (views/letsubs [username [:get-contact-name-by-identity from]]
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
      text]]))

(def regx-url #"(?i)(?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9\-]+[.][a-z]{1,4}/?)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:\'\".,<>?«»“”‘’]){0,}")

(def regx-tag #"#[a-z0-9\-]+")

(defn put-links-in-vector [text]
  (map #(map (fn [token]
               (cond
                 (re-matches regx-tag token) [:tag token]
                 (re-matches regx-url token)  [:link token]
                 :default (str token " ")))
             (string/split % #" "))
       (string/split text #"\n")))

(defn link-button [[link-tag link] outgoing]
  [react/touchable-highlight {:style {}
                              :on-press #(case link-tag
                                           :link (.openURL react/linking (http/normalize-url link))
                                           :tag (re-frame/dispatch [:chat.ui/start-public-chat (subs link 1)]))}
   [react/text {:style {:font-size 14
                        :text-decoration-line :underline
                        :color (if outgoing colors/white colors/blue)
                        :padding-bottom 1
                        :margin-right 5}}
    link]])

(defn- message-sent? [user-statuses current-public-key]
  (not= (get-in user-statuses [current-public-key :status]) :not-sent))

(views/defview message-without-timestamp
  [text {:keys [message-id content current-public-key user-statuses]} style]
  [react/view {:flex 1 :margin-vertical 5}
   [react/touchable-highlight {:on-press #(if (= "right" (.-button (.-nativeEvent %)))
                                            (do (utils/show-popup "" "Message copied to clipboard")
                                                (react/copy-to-clipboard text))
                                            (when (message-sent? user-statuses current-public-key)
                                              (re-frame/dispatch [:chat.ui/reply-to-message message-id])))}
    [react/view {:style styles/message-container}
     (when (:response-to content)
       [quoted-message (:response-to content) false current-public-key])
     [react/view {:flex-direction :column}
      (doall
       (for [[index-sentence sentence] (map-indexed vector (put-links-in-vector text))]
         ^{:key (str message-id index-sentence)}
         [react/view {:style {:flex-direction :row
                              :flex-wrap      :wrap}}
          (doall
           (for [[index word] (map-indexed vector sentence)]
             (if (vector? word)
               ^{:key (str message-id index-sentence index)}
               [link-button word false]
               ^{:key (str message-id index-sentence index)}
               [react/text {:style (styles/message-text false)}
                word])))]))]]]])

(views/defview photo-placeholder []
  [react/view {:style {:width             40
                       :margin-horizontal 16}}])

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

(defmethod message :default
  [text me? {:keys [message-id chat-id message-status user-statuses from
                    current-public-key content-type outgoing type value] :as message}]
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
          [react/view
           [message-with-name-and-avatar text message]
           [react/view {:style (message.style/delivery-status outgoing)}
            [message/message-delivery-status message]]])}))))

(def load-step 5)

(defn load-more [all-messages-count messages-to-load]
  (let [next-count (min all-messages-count (+ @messages-to-load load-step))]
    (reset! messages-to-load next-count)))

(views/defview messages-view [{:keys [chat-id group-chat]}]
  (views/letsubs [messages [:get-current-chat-messages-stream]
                  current-public-key [:get-current-public-key]
                  messages-to-load (reagent/atom load-step)
                  chat-id* (reagent/atom nil)]
    {:component-did-update #(load-more (count messages) messages-to-load)
     :component-did-mount  #(load-more (count messages) messages-to-load)}
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
       [connectivity/error-view]])))

(views/defview send-button [inp-ref]
  (views/letsubs [{:keys [input-text]} [:get-current-chat]]
    (let [empty? (= "" input-text)]
      [react/touchable-highlight {:style    styles/send-button
                                  :on-press (fn []
                                              (.clear @inp-ref)
                                              (.focus @inp-ref)
                                              (re-frame/dispatch [:chat.ui/send-current-message]))}
       [react/view {:style (styles/send-icon empty?)}
        [icons/icon :icons/arrow-left {:style (styles/send-icon-arrow empty?)}]]])))

(views/defview reply-message [from message-text]
  (views/letsubs [username           [:get-contact-name-by-identity from]
                  current-public-key [:get-current-public-key]]
    [react/view {:style styles/reply-content-container}
     [react/text {:style styles/reply-content-author}
      (chat-utils/format-reply-author from username current-public-key)]
     [react/text {:style styles/reply-content-message} message-text]]))

(views/defview reply-member-photo [from]
  (views/letsubs [photo-path [:get-photo-path from]]
    [react/image {:source {:uri (if (string/blank? photo-path)
                                  (identicon/identicon from)
                                  photo-path)}
                  :style  styles/reply-photo-style}]))

(views/defview reply-message-view []
  (views/letsubs [{:keys [content from] :as message} [:get-reply-message]]
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
  (views/letsubs [inp-ref (atom nil)]
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
                          :on-key-press           (fn [e]
                                                    (let [native-event (.-nativeEvent e)
                                                          key          (.-key native-event)
                                                          modifiers    (js->clj (.-modifiers native-event))
                                                          should-send  (and (= key "Enter") (not (contains? (set modifiers) "shift")))]
                                                      (when should-send
                                                        (.clear @inp-ref)
                                                        (.focus @inp-ref)
                                                        (re-frame/dispatch [:chat.ui/send-current-message]))))
                          :on-change              (fn [e]
                                                    (let [native-event (.-nativeEvent e)
                                                          text         (.-text native-event)]
                                                      (re-frame/dispatch [:chat.ui/set-chat-input-text text])))}]
       [send-button inp-ref]])))

(views/defview chat-view []
  (views/letsubs [{:keys [input-text chat-id] :as current-chat} [:get-current-chat]]
    [react/view {:style styles/chat-view}
     [toolbar-chat-view current-chat]
     [react/view {:style styles/separator}]
     [messages-view current-chat]
     [react/view {:style styles/separator}]
     [reply-message-view]
     [chat-text-input chat-id input-text]]))

(views/defview chat-profile []
  (views/letsubs [identity        [:get-current-contact-identity]
                  maybe-contact   [:get-current-contact]]
    (let [contact (or maybe-contact (utils.contacts/whisper-id->new-contact identity))
          {:keys [pending? whisper-identity]} contact]
      [react/view {:style styles/chat-profile-body}
       [profile.views/profile-badge contact]
       ;; for private chat, public key will be chat-id
       [react/view
        (if (or (nil? pending?) pending?)
          [react/touchable-highlight {:on-press #(re-frame/dispatch [:contact.ui/add-to-contact-pressed whisper-identity])}
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
                      [:contact.ui/send-message-pressed {:whisper-identity whisper-identity}])}
         [react/view {:style styles/chat-profile-row}
          [react/view {:style styles/chat-profile-icon-container
                       :accessibility-label :send-message-link}
           [vector-icons/icon :icons/chats {:style (styles/chat-profile-icon colors/blue)}]]
          [react/text {:style (styles/contact-card-text colors/blue)}
           (i18n/label :t/send-message)]]]
        [react/text {:style styles/chat-profile-contact-code} (i18n/label :t/contact-code)]
        [react/text {:style           {:font-size 14}
                     :selectable      true
                     :selection-color colors/blue} whisper-identity]]])))
