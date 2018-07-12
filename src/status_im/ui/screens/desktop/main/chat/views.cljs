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
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.chat.views.message.datemark :as message.datemark]
            [status-im.ui.screens.desktop.main.chat.styles :as styles]
            [status-im.ui.screens.desktop.main.tabs.home.styles :as home.styles]
            [status-im.i18n :as i18n]))

(views/defview toolbar-chat-view []
  (views/letsubs [{:keys [chat-id name public-key public? group-chat color]} [:get-current-chat]
                  {:keys [pending? whisper-identity photo-path]}             [:get-current-chat-contact]]
    (let [chat-name (str
                     (if public? "#" "")
                     (if (string/blank? name)
                       (gfycat.core/generate-gfy public-key)
                       (or name
                           (i18n/label :t/chat-name))))]
      [react/view {:style styles/toolbar-chat-view}
       [react/view {:style {:flex-direction :row
                            :align-items :center}}
        [react/view {:style styles/img-container}
         (if public?
          [react/view {:style (home.styles/topic-image color)}
           [react/text {:style home.styles/topic-text}
            (string/capitalize (first name))]]
          [react/image {:style styles/photo-style-toolbar
                        :source {:uri photo-path}}])]
        [react/view
         (when public?
           [icons/icon :icons/public-chat])
         (when (and group-chat (not public?))
           [icons/icon :icons/group-chat])
         [react/text {:style styles/toolbar-chat-name}
          chat-name]
         (when pending?
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [:add-contact whisper-identity])}
          [react/view {:style styles/add-contact}
           [react/text {:style styles/add-contact-text}
            (i18n/label :t/add-to-contacts)]]])]]])))

(views/defview message-author-name [{:keys [outgoing from] :as message}]
  (views/letsubs [current-account [:get-current-account]
                  incoming-name   [:get-contact-name-by-identity from]]
    (let [name (or incoming-name (gfycat/generate-gfy from))]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-contact-dialog from name (boolean incoming-name)])}
       [react/text {:style styles/author} name]])))

(views/defview member-photo [from]
  [react/view
   [react/image {:source {:uri (identicon/identicon from)}
                 :style  styles/photo-style}]])

(views/defview my-photo [from]
  (views/letsubs [account [:get-current-account]]
    (let [{:keys [photo-path]} account]
      [react/view
       [react/image {:source {:uri (if (string/blank? photo-path)
                                     (identicon/identicon from)
                                     photo-path)}
                     :style  styles/photo-style}]])))

(views/defview message-with-timestamp [text {:keys [timestamp] :as message} style]
  [react/view {:style style}
   [react/view {:style {:flex-direction :row :flex-wrap :wrap}}
    [react/text {:style      styles/message-text
                 :selectable true}
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
   [react/view {:style {:flex-direction :column}}
    (when first-in-group?
      [message-author-name message])
    [react/view {:style {:flex-direction :row}}
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
    (when (= content-type constants/text-content-type)
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
                           :on-scroll              (fn [e]
                                                     (let [ne (.-nativeEvent e)
                                                           y (.-y (.-contentOffset ne))]
                                                       (when (zero? y)
                                                         (when @scroll-timer (js/clearTimeout @scroll-timer))
                                                         (reset! scroll-timer (js/setTimeout #(re-frame/dispatch [:load-more-messages]) 300)))
                                                       (reset! scroll-height (+ y (.-height (.-layoutMeasurement ne))))))
                           :on-content-size-change #(.scrollToEnd @scroll-ref)
                           :ref                    #(reset! scroll-ref %)}
        [react/view {:style {:padding-vertical 46}}
         (doall
          (for [[index {:keys [from content message-id type value] :as message-obj}] (map-indexed vector (reverse @messages))]
            ^{:key (or message-id (str type value))}
            [message content (= from @current-public-key) (assoc message-obj :group-chat group-chat)]))]]])))

(views/defview chat-text-input []
  (views/letsubs [inp-ref (atom nil)]
    [react/view {:style styles/chat-box}
     [react/view {:style {:flex-direction :row :flex 1}}
      [react/view {:style {:flex 1}}
       [react/text-input {:placeholder    (i18n/label :t/type-a-message)
                          :auto-focus     true
                          :multiline      true
                          :blur-on-submit true
                          :style          styles/chat-text-input
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
     [toolbar-chat-view]
     [messages-view current-chat]
     [chat-text-input]]))
