(ns status-im.ui.screens.desktop.main.chat.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [clojure.string :as string]
            [status-im.chat.styles.message.message :as message.style]
            [status-im.utils.gfycat.core :as gfycat.core]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.constants :as constants]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.datetime :as time]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.react :as react]))

(views/defview toolbar-chat-view []
  (views/letsubs [{:keys [name public? group-chat]} [:get-current-chat]
                  chat-id [:get-current-chat-id]
                  pending-contact? [:current-contact :pending?]
                  public-key [:chat :public-key]]
    (let [chat-name (str
                      (if public? "#" "")
                      (if (string/blank? name)
                        (gfycat.core/generate-gfy public-key)
                        (or name
                            "Chat name")))]
      [react/view {:style {:align-items :center :padding 11 :justify-content :center}}
       [react/view {:style {:flex-direction :row}}
        (when public?
          [icons/icon :icons/public-chat])
        (when (and group-chat (not public?))
          [icons/icon :icons/group-chat])
        [react/text {:style {:font-size 16 :color :black :font-weight "600"}}
         chat-name]]
       (when pending-contact?
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [:add-pending-contact chat-id])}
          [react/view {:style {:background-color :white :border-radius 6 :margin-top 3 :padding 4}}                                      ;style/add-contact
           [react/text {:style {:font-size 14 :color "#939ba1"}}
            "Add to contacts"]]])])))

(views/defview message-author-name [{:keys [outgoing from] :as message}]
  (views/letsubs [current-account [:get-current-account]
                  incoming-name [:contact-name-by-identity from]]
    (if outgoing
      [react/text {:style message.style/author} (:name current-account)]
      (let [name (or incoming-name (gfycat/generate-gfy from))]
        [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-contact-dialog from name (boolean incoming-name)])}
         [react/text {:style message.style/author} name]]))))

(def photo-style
  {:borderRadius 15
   :width        30
   :height       30})

(views/defview member-photo [from]
  (views/letsubs [photo-path nil];[:photo-path from]]
    [react/view
     [react/image {:source {:uri (if (string/blank? photo-path)
                                   (identicon/identicon from)
                                   photo-path)}
                   :style  photo-style}]]))

(views/defview my-photo [from]
  (views/letsubs [account [:get-current-account]]
    (let [{:keys [photo-path]} account]
      [react/view
       [react/image {:source {:uri (if (string/blank? photo-path)
                                     (identicon/identicon from)
                                     photo-path)}
                     :style  photo-style}]])))

(defn message [text me? {:keys [outgoing message-id chat-id message-status user-statuses timestamp
                                from current-public-key content-type group-chat type value] :as message}]
  (if (= type :datemark)
    ^{:key (str "datemark" message-id)}
    [react/view {:style {:margin-vertical 20}}
     [react/text
      (string/capitalize (last (string/split value #"-")))]
     [react/view {:style {:height 1 :background-color "#e8ebec"}}]]
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
           [react/view {:style {:flex-direction :row :flex 1 :margin-vertical 12}}
            (if outgoing
              [my-photo from]
              [member-photo from])
            [react/view {:style {:padding-horizontal 12 :background-color :white :border-radius 8 :flex 1}}
             [react/view {:style {:flex-direction :row}}
              [message-author-name message]
              [react/view {:style {:flex 1}}]
              [react/text {:style {:color styles/color-gray4 :font-size 12}} (time/timestamp->time timestamp)]]
             ;;TODO use https://github.com/status-im/status-react/pull/3299
             ;;[rn-hl/hyperlink {:linkStyle {:color "#2980b9"} :on-press #(re-frame/dispatch [:show-link-dialog %1])}
             [react/text
              text]]])}))))

(views/defview messages-view [{:keys [chat-id group-chat]}]
  (views/letsubs [chat-id* (atom nil)
                  scroll-ref (atom nil)
                  scroll-timer (atom nil)
                  scroll-height (atom nil)]
    (let [_ (when (or (not @chat-id*) (not= @chat-id* chat-id))
              (reset! chat-id* chat-id)
              (js/setTimeout #(when scroll-ref (.scrollToEnd @scroll-ref)) 400))
          messages (re-frame/subscribe [:get-current-chat-messages])
          current-public-key (re-frame/subscribe [:get-current-public-key])]
      [react/view {:style {:flex 1 :background-color :white :margin-horizontal 16}}
       [react/scroll-view {:scrollEventThrottle    16
                           :on-scroll              (fn [e]
                                                    (let [ne (.-nativeEvent e)
                                                          y (.-y (.-contentOffset ne))]
                                                      (when (zero? y)
                                                        (when @scroll-timer (js/clearTimeout @scroll-timer))
                                                        (reset! scroll-timer (js/setTimeout #(re-frame/dispatch [:load-more-messages]) 300)))
                                                      (reset! scroll-height (+ y (.-height (.-layoutMeasurement ne))))))
                           :on-content-size-change #(when (or (not @scroll-height) (< (- %2 @scroll-height) 500))
                                                      (.scrollToEnd @scroll-ref))
                           :ref                    #(reset! scroll-ref %)}
        [react/view {:style {:padding-vertical 60}}
         (doall
           (for [[index {:keys [from content message-id] :as message-obj}] (map-indexed vector (reverse @messages))]
             ^{:key (str message index)}
             [message content (= from @current-public-key) (assoc message-obj :group-chat group-chat)]))]]])))

(views/defview chat-text-input []
  (views/letsubs [inp-ref (atom nil)]
    [react/view {:style {:height 90 :margin-horizontal 16  :background-color :white :border-radius 12}}
     [react/view {:style {:flex-direction :row :margin-horizontal 16 :margin-top 16 :flex 1 :margin-bottom 16}}
      [react/view {:style {:flex 1}}
       [react/text-input {:placeholder    "Type a message..."
                          :auto-focus     true
                          :multiline      true
                          :blur-on-submit true
                          :style          {:flex 1}
                          :ref            #(reset! inp-ref %)
                          :on-key-press   (fn [e]
                                            (let [native-event (.-nativeEvent e)
                                                  key (.-key native-event)]
                                              (when (= key "Enter")
                                                (js/setTimeout #(do
                                                                  (.clear @inp-ref)
                                                                  (.focus @inp-ref)) 200)
                                                (re-frame/dispatch [:send-current-message]))))
                          :on-change      (fn [e]
                                            (let [native-event (.-nativeEvent e)
                                                  text (.-text native-event)]
                                              (re-frame/dispatch [:set-chat-input-text text])))}]]
      [react/touchable-highlight {:on-press (fn []
                                              (js/setTimeout #(do (.clear @inp-ref)(.focus @inp-ref)) 200)
                                              (re-frame/dispatch [:send-current-message]))}
       [react/view {:style {:margin-left     16 :width 30 :height 30 :border-radius 15 :background-color "#eef2f5" :align-items :center
                            :justify-content :center :transform [{ :rotate "90deg"}]}}
        [icons/icon :icons/arrow-left]]]]]))

(views/defview chat-view []
  (views/letsubs [current-chat [:get-current-chat]]
    [react/view {:style {:flex 1 :background-color :white}}
     [toolbar-chat-view]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [messages-view current-chat]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [chat-text-input]]))