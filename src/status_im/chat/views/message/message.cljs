(ns status-im.chat.views.message.message
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.walk :as walk]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.chat.models.commands :as commands]
            [status-im.commands.utils :as commands.utils]
            [status-im.chat.utils :as chat.utils]
            [status-im.chat.styles.message.message :as style]
            [status-im.chat.styles.message.command-pill :as pill-style]
            [status-im.chat.views.message.request-message :as request-message]
            [status-im.constants :as constants]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.utils.events-buffer :as events-buffer]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [clojure.string :as string]
            [status-im.chat.events.console :as console]
            [taoensso.timbre :as log]))

(def window-width (:width (react/get-dimensions "window")))

(defview message-author-name [{:keys [outgoing from username] :as message}]
  (letsubs [current-account [:get-current-account]
            incoming-name   [:contact-name-by-identity from]]
    (when-let [name (if outgoing
                      (:name current-account)
                      (or incoming-name username (gfycat/generate-gfy from)))]
      [react/text {:style style/author} name])))

(defview message-content-status []
  (letsubs [{:keys [chat-id group-id name color public-key]} [:get-current-chat]
            members                                          [:current-chat-contacts]]
    (let [{:keys [status]} (if group-id
                             {:status nil}
                             (first members))]
      [react/view style/status-container
       [chat-icon.screen/chat-icon-message-status chat-id group-id name color false]
       [react/text {:style           style/status-from
                    :font            :default
                    :number-of-lines 1}
        (if (string/blank? name)
          (gfycat/generate-gfy public-key)
          (or (i18n/get-contact-translated chat-id :name name)
              (i18n/label :t/chat-name)))]
       (when status
         [react/text {:style style/status-text
                      :font  :default}
          status])])))

(defn message-content-audio [_]
  [react/view style/audio-container
   [react/view style/play-view
    [react/image {:style  style/play-image}]]
   [react/view style/track-container
    [react/view style/track]
    [react/view style/track-mark]
    [react/text {:style style/track-duration-text
                 :font  :default}
     "03:39"]]])

(defview message-content-command
  [{:keys [content params] :as message}]
  (letsubs [command [:get-command (:content-command-ref content)]]
    {:component-will-mount #(when-not (:preview content)
                              (re-frame/dispatch [:request-command-message-data
                                                  message {:data-type   :preview
                                                           :cache-data? true}]))}
    (let [preview (:preview content)
          {:keys [type color] icon-path :icon} command]
      [react/view style/content-command-view
       (when color
         [react/view style/command-container
          [react/view (pill-style/pill command)
           [react/text {:style pill-style/pill-text
                        :font  :default}
            (chat.utils/command-name command)]]])
       (when icon-path
         [react/view style/command-image-view
          [react/icon icon-path style/command-image]])
       (if (:markup preview)
         ;; Markup was defined for command in jail, generate hiccup and render it
         (commands.utils/generate-hiccup (:markup preview))
         ;; Display preview if it's defined (as a string), in worst case, render params
         [react/text {:style style/command-text
                      :font  :default}
          (or preview (str params))])])))

(defn message-view
  [{:keys [group-chat] :as message} content]
  [react/view (style/message-view message)
   (when group-chat [message-author-name message])
   content])

(def replacements
  {"\\*[^*]+\\*" {:font-weight :bold}
   "~[^~]+~"     {:font-style :italic}})

(def regx (re-pattern (string/join "|" (map first replacements))))

(defn get-style [string]
  (->> replacements
       (into [] (comp (map first)
                      (map #(vector % (re-pattern %)))
                      (drop-while (fn [[_ regx]] (not (re-matches regx string))))
                      (take 1)))
       ffirst
       replacements))

;; todo rewrite this, naive implementation
(defn- parse-text [string]
  (if (string? string)
    (let [general-text  (string/split string regx)
          general-text' (if (zero? (count general-text))
                          [nil]
                          general-text)
          styled-text   (vec (map-indexed (fn [idx string]
                                            (let [style (get-style string)]
                                              [react/text
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
       [react/autolink {:style   (style/text-message message)
                        :text    (apply str parsed-text)
                        :onPress #(re-frame/dispatch [:browse-link-from-message %])}]
       [react/text {:style (style/text-message message)} parsed-text]))])

(defmulti message-content (fn [_ message _] (message :content-type)))

(defmethod message-content constants/content-type-command-request
  [wrapper message]
  [wrapper message
   [message-view message [request-message/message-content-command-request message]]])

(defmethod message-content constants/text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content constants/content-type-log-message
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content constants/content-type-status
  [_ message]
  [message-content-status])

(defmethod message-content constants/content-type-command
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

(defmethod message-content :default
  [wrapper {:keys [content-type content] :as message}]
  [wrapper message
   [message-view message
    [message-content-audio {:content      content
                            :content-type content-type}]]])

(defn- text-status [status]
  [react/view style/delivery-view
   [react/text {:style style/delivery-text
                :font  :default}
    (i18n/message-status-label status)]])

(defview group-message-delivery-status [{:keys [message-id group-id current-public-key user-statuses] :as msg}]
  (letsubs [{participants :contacts} [:get-current-chat]
            contacts                 [:get-contacts]]
    (let [outgoing-status         (or (get user-statuses current-public-key) :sending)
          delivery-statuses       (dissoc user-statuses current-public-key)
          delivery-statuses-count (count delivery-statuses)
          seen-by-everyone        (and (= delivery-statuses-count (count participants))
                                       (every? (comp (partial = :seen) second) delivery-statuses)
                                       :seen-by-everyone)]
      (if (or seen-by-everyone (zero? delivery-statuses-count))
        [text-status (or seen-by-everyone outgoing-status)]
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:show-message-details {:message-status outgoing-status
                                                                :user-statuses  delivery-statuses
                                                                :participants   participants}])}
         [react/view style/delivery-view
          (for [[whisper-identity] (take 3 delivery-statuses)]
            ^{:key whisper-identity}
            [react/image {:source {:uri (or (get-in contacts [whisper-identity :photo-path])
                                            (identicon/identicon whisper-identity))}
                          :style  {:width        16
                                   :height       16
                                   :borderRadius 8}}])
          (if (> delivery-statuses-count 3)
            [react/text {:style style/delivery-text
                         :font  :default}
             (str "+ " (- delivery-statuses-count 3))])]]))))

(defn message-delivery-status
  [{:keys [message-id chat-id current-public-key user-statuses content]}]
  (let [outgoing-status (or (get user-statuses current-public-key) :sending)
        delivery-status (get user-statuses chat-id)
        status          (cond (and (= constants/console-chat-id chat-id)
                                   (not (console/commands-with-delivery-status (:command content))))
                              :seen

                              :else
                              (or delivery-status outgoing-status))]
    [text-status status]))

(defn- photo [from photo-path]
  [react/view
   [react/image {:source {:uri (if (string/blank? photo-path)
                                 (identicon/identicon from)
                                 photo-path)}
                 :style  style/photo}]])

(defview member-photo [from]
  (letsubs [photo-path [:get-photo-path from]]
    (photo from photo-path)))

(defview my-photo [from]
  (letsubs [{:keys [photo-path]} [:get-current-account]]
    (photo from photo-path)))

(defn message-body
  [{:keys [last-outgoing? message-type same-author? from outgoing group-chat] :as message} content]
  [react/view style/group-message-wrapper
   [react/view (style/message-body message)
    [react/view style/message-author
     (when-not same-author?
       (if outgoing
         [my-photo from]
         [member-photo from]))]
    [react/view (style/group-message-view message)
     content
     (when last-outgoing?
       (if (or (= (keyword message-type) :group-user-message)
               group-chat)
         [group-message-delivery-status message]
         [message-delivery-status message]))]]])

(defn message-container-animation-logic [{:keys [to-value val callback]}]
  (fn [_]
    (let [to-value @to-value]
      (when (pos? to-value)
        (animation/start
          (animation/timing val {:toValue  to-value
                                 :duration 250})
          (fn [arg]
            (when (.-finished arg)
              (callback))))))))

(defn message-container [message & children]
  (if (:new? message)
    (let [layout-height (reagent/atom 0)
          anim-value    (animation/create-value 1)
          anim-callback #(events-buffer/dispatch [:set-message-shown message])
          context       {:to-value layout-height
                         :val      anim-value
                         :callback anim-callback}
          on-update     (message-container-animation-logic context)]
      (reagent/create-class
        {:component-did-update
         on-update
         :display-name "message-container"
         :reagent-render
         (fn [_ & children]
           @layout-height
           [react/animated-view {:style (style/message-animated-container anim-value)}
            (into [react/view {:style    (style/message-container window-width)
                               :onLayout (fn [event]
                                           (let [height (.. event -nativeEvent -layout -height)]
                                             (reset! layout-height height)))}]
                  children)])}))
    (into [react/view] children)))

(defn chat-message [{:keys [outgoing message-id chat-id from current-public-key] :as message}]
  (reagent/create-class
    {:display-name
     "chat-message"
     :component-did-mount
     ;; send `:seen` signal when we have signed-in user, message not from us and we didn't sent it already
     (fn []
       (when (and current-public-key message-id chat-id (not outgoing)
                  (not (chat.utils/message-seen-by? message current-public-key)))
         (events-buffer/dispatch [:send-seen! {:chat-id    chat-id
                                               :from       from
                                               :me         current-public-key
                                               :message-id message-id}])))
     :reagent-render
     (fn [{:keys [outgoing group-chat content-type content] :as message}]
       [message-container message
        [react/touchable-highlight {:on-press      #(when platform/ios?
                                                      (re-frame/dispatch [:set-chat-ui-props
                                                                          {:show-emoji? false}])
                                                      (react/dismiss-keyboard!))
                                    :on-long-press #(cond (= content-type constants/text-content-type)
                                                          (list-selection/share content (i18n/label :t/message))
                                                          (and (= content-type constants/content-type-command)
                                                               (= "location" (:content-command content)))
                                                          (let [address (get-in content [:params :address])
                                                                [location lat long] (string/split address #"&amp;")]
                                                            (list-selection/share-or-open-map location lat long)))}
         [react/view
          (let [incoming-group (and group-chat (not outgoing))]
            [message-content message-body (merge message
                                                 {:current-public-key current-public-key
                                                  :incoming-group     incoming-group})])]]])}))
