(ns status-im.chat.views.message.message
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [clojure.walk :as walk]
            [reagent.core :as r]
            [status-im.i18n :refer [message-status-label]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                animated-view
                                                touchable-without-feedback
                                                touchable-highlight
                                                autolink
                                                get-dimensions
                                                dismiss-keyboard!]]
            [status-im.components.animation :as anim]
            [status-im.components.list-selection :refer [share browse share-or-open-map]]
            [status-im.chat.constants :as chat-consts]
            [status-im.chat.models.commands :as commands]
            [status-im.chat.styles.message.message :as st]
            [status-im.chat.styles.message.command-pill :as pill-st]
            [status-im.chat.views.message.request-message :refer [message-content-command-request]]
            [status-im.chat.views.message.datemark :refer [chat-datemark]]
            [status-im.react-native.resources :as res]
            [status-im.constants :refer [console-chat-id
                                         wallet-chat-id
                                         text-content-type
                                         content-type-log-message
                                         content-type-status
                                         content-type-command
                                         content-type-command-request] :as c]
            [status-im.components.chat-icon.screen :refer [chat-icon-message-status]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.utils.platform :as platform]
            [status-im.i18n :refer [label
                                    get-contact-translated]]
            [status-im.chat.utils :as cu]
            [clojure.string :as str]
            [status-im.chat.events.console :as console]
            [taoensso.timbre :as log]))

(def window-width (:width (get-dimensions "window")))

(defview message-author-name [{:keys [outgoing from] :as message}]
  [current-account [:get-current-account]
   incoming-name [:contact-name-by-identity from]]
  (if-let [name (if outgoing
                  (:name current-account)
                  (or incoming-name "Unknown contact"))]
    [text {:style st/author} name]))

(defview message-content-status
  [{:keys [messages-count content datemark]}]
  (letsubs [chat-id    [:chat :chat-id]
            group-chat [:chat :group-id]
            name       [:chat :name]
            color      [:chat :color]
            public-key [:chat :public-key]
            members    [:current-chat-contacts]]
    (let [{:keys [status]} (if group-chat
                             {:photo-path  nil
                              :status      nil
                              :last-online 0}
                             (first members))]
      [view st/status-container
       [chat-icon-message-status chat-id group-chat name color false]
       [text {:style           st/status-from
              :font            :default
              :number-of-lines 1}
        (if (str/blank? name)
          (generate-gfy public-key)
          (or (get-contact-translated chat-id :name name)
              (label :t/chat-name)))]
       (when (or status content)
         [text {:style st/status-text
                :font  :default}
          (or status content)])
       (if (> messages-count 1)
         [view st/message-datemark
          [chat-datemark datemark]]
         [view st/message-empty-spacing])])))

(defn message-content-audio [_]
  [view st/audio-container
   [view st/play-view
    [image {;:source res/play
            :style  st/play-image}]]
   [view st/track-container
    [view st/track]
    [view st/track-mark]
    [text {:style st/track-duration-text
           :font  :default}
     "03:39"]]])

(defn wallet-command-preview
  [{{:keys [name]} :contact-chat
    :keys          [contact-address params outgoing? current-chat-id]}]
  (let [{:keys [recipient amount]} (walk/keywordize-keys params)]
    [text {:style st/command-text
           :font  :default}
     (if (= current-chat-id wallet-chat-id)
       (let [label-val (if outgoing? :t/chat-send-eth-to :t/chat-send-eth-from)]
         (label label-val {:amount    amount
                           :chat-name (or name contact-address recipient)}))
       (label :t/chat-send-eth {:amount amount}))]))

(defn wallet-command? [content-type]
  (#{c/content-type-wallet-command c/content-type-wallet-request} content-type))

(defn command-preview
  [{:keys [params preview content-type] :as message}]
  (cond
    (wallet-command? content-type)
    (wallet-command-preview message)

    preview preview

    :else
    [text {:style st/command-text
           :font  :default}
     (if (= 1 (count params))
       (first (vals params))
       (str params))]))

(defview message-content-command
  [{:keys [message-id content content-type chat-id to from outgoing] :as message}]
  [commands [:get-commands-and-responses chat-id]
   from-commands [:get-commands-and-responses from]
   global-commands [:get :global-commands]
   current-chat-id [:get-current-chat-id]
   contact-chat [:get-in [:chats (if outgoing to from)]]
   preview [:get-message-preview message-id]]
  (let [commands (merge commands from-commands)
        {:keys [command params]} (commands/set-command-for-content commands global-commands content)
        {:keys     [name type]
         icon-path :icon} command]
    [view st/content-command-view
     (when (:color command)
       [view st/command-container
        [view (pill-st/pill command)
         [text {:style pill-st/pill-text
                :font  :default}
          (str (if (= :command type) chat-consts/command-char "?") name)]]])
     (when icon-path
       [view st/command-image-view
        [icon icon-path st/command-image]])
     [command-preview {:command         (:name command)
                       :content-type    content-type
                       :params          params
                       :outgoing?       outgoing
                       :preview         preview
                       :contact-chat    contact-chat
                       :contact-address (if outgoing to from)
                       :current-chat-id current-chat-id}]]))

(defn message-view
  [{:keys [same-author index group-chat] :as message} content]
  [view (st/message-view message)
   (when group-chat [message-author-name message])
   content])

(def replacements
  {"\\*[^*]+\\*" {:font-weight :bold}
   "~[^~]+~"     {:font-style :italic}})

(def regx (re-pattern (str/join "|" (map first replacements))))

(defn get-style [string]
  (->> replacements
       (into [] (comp
                  (map first)
                  (map #(vector % (re-pattern %)))
                  (drop-while (fn [[_ regx]] (not (re-matches regx string))))
                  (take 1)))
       ffirst
       replacements))

;; todo rewrite this, naive implementation
(defn- parse-text [string]
  (if (string? string)
    (let [general-text  (str/split string regx)
          general-text' (if (zero? (count general-text))
                          [nil]
                          general-text)
          styled-text   (vec (map-indexed
                               (fn [idx string]
                                 (let [style (get-style string)]
                                   [text
                                    {:key   (str idx "_" string)
                                     :style style}
                                    (subs string 1 (dec (count string)))]))
                               (re-seq regx string)))
          styled-text'  (if (> (count general-text)
                               (count styled-text))
                          (conj styled-text nil)
                          styled-text)]
      (mapcat vector general-text' styled-text'))
    (str string)))

(defn text-message
  [{:keys [content] :as message}]
  [message-view message
   (let [parsed-text  (parse-text content)
         simple-text? (and (= (count parsed-text) 2)
                           (nil? (second parsed-text)))]
     (if simple-text?
       [autolink {:style   (st/text-message message)
                  :text    (apply str parsed-text)
                  :onPress #(browse %)}]
       [text {:style (st/text-message message)} parsed-text]))])

(defmulti message-content (fn [_ message _] (message :content-type)))

(defmethod message-content content-type-command-request
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command-request message]]])

(defmethod message-content c/content-type-wallet-request
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command-request message]]])

(defmethod message-content text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-log-message
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-status
  [_ message]
  [message-content-status message])

(defmethod message-content content-type-command
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

(defmethod message-content c/content-type-wallet-command
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

(defmethod message-content :default
  [wrapper {:keys [content-type content] :as message}]
  [wrapper message
   [message-view message
    [message-content-audio {:content      content
                            :content-type content-type}]]])

(defview group-message-delivery-status [{:keys [message-id group-id message-status user-statuses] :as msg}]
  [app-db-message-user-statuses [:get-in [:message-data :user-statuses message-id]]
   app-db-message-status-value [:get-in [:message-data :statuses message-id :status]]
   chat [:get-chat-by-id group-id]
   contacts [:get-contacts]]
  (let [status            (or message-status app-db-message-status-value :sending)
        user-statuses     (merge user-statuses app-db-message-user-statuses)
        participants      (:contacts chat)
        seen-by-everyone? (and (= (count user-statuses) (count participants))
                               (every? (fn [[_ {:keys [status]}]]
                                         (= (keyword status) :seen)) user-statuses))]
    (if (or (zero? (count user-statuses))
            seen-by-everyone?)
      [view st/delivery-view
       [text {:style st/delivery-text
              :font  :default}
        (message-status-label
          (if seen-by-everyone?
            :seen-by-everyone
            status))]]
      [touchable-highlight
       {:on-press (fn []
                    (dispatch [:show-message-details {:message-status status
                                                      :user-statuses  user-statuses
                                                      :participants   participants}]))}
       [view st/delivery-view
        (for [[_ {:keys [whisper-identity]}] (take 3 user-statuses)]
          ^{:key whisper-identity}
          [image {:source {:uri (or (get-in contacts [whisper-identity :photo-path])
                                    (identicon whisper-identity))}
                  :style  {:width        16
                           :height       16
                           :borderRadius 8}}])
        (if (> (count user-statuses) 3)
          [text {:style st/delivery-text
                 :font  :default}
           (str "+ " (- (count user-statuses) 3))])]])))

(defview message-delivery-status
  [{:keys [message-id chat-id message-status user-statuses content]}]
  [app-db-message-status-value [:get-in [:message-data :statuses message-id :status]]]
  (let [delivery-status (get-in user-statuses [chat-id :status]) 
        status          (cond (and (not (console/commands-with-delivery-status (:command content)))
                                   (cu/console? chat-id))
                              :seen

                              (cu/wallet? chat-id)
                              :sent

                              :else
                              (or delivery-status message-status app-db-message-status-value :sending))]
    [view st/delivery-view
     [text {:style st/delivery-text
            :font  :default}
      (message-status-label status)]]))

(defview member-photo [from]
  [photo-path [:photo-path from]]
  [view
   [image {:source {:uri (if (str/blank? photo-path)
                           (identicon from)
                           photo-path)}
           :style  st/photo}]])

(defview my-photo [from]
  [account [:get-current-account]]
  (let [{:keys [photo-path]} account]
    [view
     [image {:source {:uri (if (str/blank? photo-path)
                             (identicon from)
                             photo-path)}
             :style  st/photo}]]))

(defn message-body
  [{:keys [last-outgoing? message-type same-author from index outgoing] :as message} content]
  (let [delivery-status :seen-by-everyone]
    [view st/group-message-wrapper
     [view (st/message-body message)
      [view st/message-author
       (when (or (= index 1) (not same-author))
         (if outgoing
           [my-photo from]
           [member-photo from]))]
      [view (st/group-message-view message)
       content
       (when last-outgoing?
         (if (= (keyword message-type) :group-user-message)
           [group-message-delivery-status message]
           [message-delivery-status message]))]]]))

(defn message-container-animation-logic [{:keys [to-value val callback]}]
  (fn [_]
    (let [to-value @to-value]
      (when (pos? to-value)
        (anim/start
          (anim/timing val {:toValue  to-value
                            :duration 250})
          (fn [arg]
            (when (.-finished arg)
              (callback))))))))

(defn message-container [message & children]
  (if (:new? message)
    (let [layout-height (r/atom 0)
          anim-value    (anim/create-value 1)
          anim-callback #(dispatch [:set-message-shown message])
          context       {:to-value layout-height
                         :val      anim-value
                         :callback anim-callback}
          on-update     (message-container-animation-logic context)]
      (r/create-class
        {:component-did-update
         on-update
         :display-name "message-container"
         :reagent-render
         (fn [_ & children]
           @layout-height
           [animated-view {:style (st/message-animated-container anim-value)}
            (into [view {:style    (st/message-container window-width)
                         :onLayout (fn [event]
                                     (let [height (.. event -nativeEvent -layout -height)]
                                       (reset! layout-height height)))}]
                  children)])}))
    (into [view] children)))

(defn chat-message [{:keys [outgoing message-id chat-id user-statuses from] :as message}]
  (let [my-identity (subscribe [:get :current-public-key])
        status      (subscribe [:get-in [:message-data :user-statuses message-id my-identity]])
        preview     (subscribe [:get-message-preview message-id])]
    (r/create-class
      {:display-name "chat-message"
       :component-will-mount
       (fn []
         (let [{:keys [bot command] :as content} (get-in message [:content])
               message' (assoc message :jail-id bot)]
           (when (and command (not @preview))
             (dispatch [:request-command-preview message']))))

       :component-did-mount
       (fn []
         (when (and (not outgoing)
                    (not= :seen (keyword @status))
                    (not= :seen (keyword (get-in user-statuses [@my-identity :status]))))
           (dispatch [:send-seen! {:chat-id    chat-id
                                   :from       from
                                   :message-id message-id}])))
       :reagent-render
       (fn [{:keys [outgoing group-chat content-type content] :as message}]
         [message-container message
          [touchable-highlight {:on-press #(when platform/ios? (dismiss-keyboard!))
                                :on-long-press #(cond (= content-type text-content-type)
                                                      (share content (label :t/message))
                                                      (and (= content-type content-type-command) (= "location" (:content-command content)))
                                                      (let [address (get-in content [:params :address])
                                                            [location lat long] (str/split address #"&amp;")]
                                                        (share-or-open-map location lat long)))}
           [view
            (let [incoming-group (and group-chat (not outgoing))]
              [message-content message-body (merge message
                                                   {:incoming-group incoming-group})])]]])})))
