(ns status-im.chat.views.message.message
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.action-sheet :as action-sheet]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.receiving :as commands-receiving]
            [status-im.chat.styles.message.message :as style]
            [status-im.chat.styles.message.command-pill :as pill-style]
            [status-im.chat.views.photos :as photos]
            [status-im.constants :as constants]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.utils.core :as utils]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [clojure.string :as string]))

(defview message-content-status []
  (letsubs [{:keys [chat-id group-id name color public-key]} [:get-current-chat]
            members                                          [:get-current-chat-contacts]]
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

(defview message-content-command
  [command-message]
  (letsubs [id->command [:get-id->command]]
    (if-let [command (commands-receiving/lookup-command-by-ref command-message id->command)]
      (commands/generate-preview command command-message)
      [react/text (str "Unhandled command: " (-> command-message :content :command-path first))])))

(def rtl-characters-regex #"[^\u0591-\u06EF\u06FA-\u07FF\u200F\u202B\u202E\uFB1D-\uFDFD\uFE70-\uFEFC]*?[\u0591-\u06EF\u06FA-\u07FF\u200F\u202B\u202E\uFB1D-\uFDFD\uFE70-\uFEFC]")

(defn right-to-left-text? [content]
  (let [char (first content)]
    (re-matches rtl-characters-regex char)))

(defview message-timestamp [t justify-timestamp? outgoing command? content]
  (when-not command?
    (let [rtl? (right-to-left-text? content)]
      [react/text {:style (style/message-timestamp-text justify-timestamp? outgoing rtl?)} t])))

(defn message-view
  [{:keys [timestamp-str outgoing content] :as message} message-content {:keys [justify-timestamp?]}]
  [react/view (style/message-view message)
   message-content
   [message-timestamp timestamp-str justify-timestamp? outgoing (or (get content :command-path)
                                                                    (get content :command-ref))
    content]])

(def replacements
  {"\\*[^*]+\\*" {:font-weight :bold}
   "~[^~]+~"     {:font-style :italic}})

(def regx-styled (re-pattern (string/join "|" (map first replacements))))

(def regx-url #"(?i)(?:[a-z][\w-]+:(?:/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9\-]+[.][a-z]{1,4}/?)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:\'\".,<>?«»“”‘’]){0,}")

(defn- parse-str-regx [string regx matched-fn unmatched-fn]
  (if (string? string)
    (let [unmatched-text (as-> (->> (string/split string regx)
                                    (remove nil?)
                                    vec) $
                           (if (zero? (count $))
                             [nil]
                             (unmatched-fn $)))
          matched-text   (as-> (->> string
                                    (re-seq regx)
                                    matched-fn
                                    vec) $
                           (if (> (count unmatched-text)
                                  (count $))
                             (conj $ nil)
                             $))]
      (mapcat vector unmatched-text matched-text))
    (str string)))

(defn parse-url [string]
  (parse-str-regx string
                  regx-url
                  (fn [text-seq]
                    (map (fn [[text]] {:text text :url? true}) text-seq))
                  (fn [text-seq]
                    (map (fn [text] {:text text :url? false}) text-seq))))

(defn- autolink [string event-on-press]
  (->> (parse-url string)
       (map-indexed (fn [idx {:keys [text url?]}]
                      (if url?
                        (let [[url _ _ _ text] (re-matches #"(?i)^((\w+://)?(www\d{0,3}[.])?)?(.*)$" text)]
                          [react/text
                           {:key      idx
                            :style    {:color colors/blue}
                            :on-press #(re-frame/dispatch [event-on-press url])}
                           url])
                        text)))
       vec))

(defn get-style [string]
  (->> replacements
       (into [] (comp (map first)
                      (map #(vector % (re-pattern %)))
                      (drop-while (fn [[_ regx]] (not (re-matches regx string))))
                      (take 1)))
       ffirst
       replacements))

;; todo rewrite this, naive implementation
(defn- parse-text [string event-on-press]
  (parse-str-regx string
                  regx-styled
                  (fn [text-seq]
                    (map-indexed (fn [idx string]
                                   (let [style (get-style string)]
                                     [react/text
                                      {:key   (str idx "_" string)
                                       :style style}
                                      (subs string 1 (dec (count string)))]))
                                 text-seq))
                  (fn [text-seq]
                    (map-indexed (fn [idx string]
                                   (apply react/text
                                          {:key (str idx "_" string)}
                                          (autolink string event-on-press)))
                                 text-seq))))

; We can't use CSS as nested Text element don't accept margins nor padding
; so we pad the invisible placeholder with some spaces to avoid having too
; close to the text.
(defn timestamp-with-padding [t]
  (str "   " t))

(def cached-parse-text (memoize parse-text))

(def ^:private ^:const number-of-lines 20)
(def ^:private ^:const number-of-chars 600)

(defn- should-collapse? [text group-chat?]
  (and group-chat?
       (or (<= number-of-chars (count text))
           (<= number-of-lines (count (re-seq #"\n" text))))))

(defn- expand-button [collapsed? on-press]
  [react/text {:style    style/message-expand-button
               :on-press on-press}
   (i18n/label (if @collapsed? :show-more :show-less))])

(defn text-message
  [{:keys [content timestamp-str group-chat outgoing] :as message}]
  [message-view message
   (let [parsed-text (cached-parse-text content :browse-link-from-message)
         ref (reagent/atom nil)
         collapsible? (should-collapse? content group-chat)
         collapsed? (reagent/atom collapsible?)
         on-press (when collapsible?
                    #(do
                       (.setNativeProps @ref
                                        (clj->js {:numberOfLines
                                                  (when-not @collapsed?
                                                    number-of-lines)}))
                       (reset! collapsed? (not @collapsed?))))]
     [react/view
      [react/text {:style           (style/text-message collapsible?)
                   :number-of-lines (when collapsible? number-of-lines)
                   :ref             (partial reset! ref)}
       parsed-text
       [react/text {:style (style/message-timestamp-placeholder-text outgoing)} (timestamp-with-padding timestamp-str)]]
      (when collapsible?
        [expand-button collapsed? on-press])])
   {:justify-timestamp? true}])

(defn emoji-message
  [{:keys [content] :as message}]
  [message-view message
   [react/text {:style (style/emoji-message message)} content]])

(defmulti message-content (fn [_ message _] (message :content-type)))

(defmethod message-content constants/text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content constants/content-type-log-message
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content constants/content-type-status
  [_ _]
  [message-content-status])

;; TODO(janherich) in the future, `content-type-command-request` will be deprecated
;; as it's the same thing as command
(defmethod message-content constants/content-type-command-request
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

(defmethod message-content constants/content-type-command
  [wrapper message]
  [wrapper message
   [message-view message [message-content-command message]]])

(defmethod message-content constants/content-type-emoji
  [wrapper message]
  [wrapper message [emoji-message message]])

(defmethod message-content :default
  [wrapper {:keys [content-type] :as message}]
  [wrapper message
   [message-view message
    [react/text {} (str "Unhandled content-type " content-type)]]])

(defn- text-status [status]
  [react/view style/delivery-view
   [react/text {:style style/delivery-text
                :font  :default}
    (i18n/message-status-label status)]])

(defview group-message-delivery-status [{:keys [message-id current-public-key user-statuses] :as msg}]
  (letsubs [{participants :contacts} [:get-current-chat]
            contacts                 [:get-contacts]]
    (let [outgoing-status         (or (get-in user-statuses [current-public-key :status]) :sending)
          delivery-statuses       (dissoc user-statuses current-public-key)
          delivery-statuses-count (count delivery-statuses)
          seen-by-everyone        (and (= delivery-statuses-count (count participants))
                                       (every? (comp (partial = :seen) :status second) delivery-statuses)
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
                          :style  {:width         16
                                   :height        16
                                   :border-radius 8}}])
          (if (> delivery-statuses-count 3)
            [react/text {:style style/delivery-text
                         :font  :default}
             (str "+ " (- delivery-statuses-count 3))])]]))))

(defn message-activity-indicator []
  [react/view style/message-activity-indicator
   [react/activity-indicator {:animating true}]])

(defn message-not-sent-text [chat-id message-id]
  [react/touchable-highlight {:on-press (fn [] (if platform/ios?
                                                 (action-sheet/show {:title   (i18n/label :message-not-sent)
                                                                     :options [{:label  (i18n/label :resend-message)
                                                                                :action #(re-frame/dispatch [:resend-message chat-id message-id])}
                                                                               {:label        (i18n/label :delete-message)
                                                                                :destructive? true
                                                                                :action       #(re-frame/dispatch [:delete-message chat-id message-id])}]})
                                                 (re-frame/dispatch
                                                  [:show-message-options {:chat-id    chat-id
                                                                          :message-id message-id}])))}
   [react/view style/not-sent-view
    [react/text {:style style/not-sent-text}
     (i18n/message-status-label :not-sent)]
    [react/view style/not-sent-icon
     [vector-icons/icon :icons/warning {:color colors/red}]]]])

(defview command-status [{{:keys [network]} :params}]
  (letsubs [current-network [:network-name]]
    (when (and network (not= current-network network))
      [react/view style/not-sent-view
       [react/text {:style style/not-sent-text}
        (i18n/label :network-mismatch)]
       [react/view style/not-sent-icon
        [vector-icons/icon :icons/warning {:color colors/red}]]])))

(defn message-delivery-status
  [{:keys [chat-id message-id current-public-key user-statuses content last-outgoing? outgoing message-type] :as message}]
  (let [outgoing-status (or (get-in user-statuses [current-public-key :status]) :not-sent)
        delivery-status (get-in user-statuses [chat-id :status])
        status          (or delivery-status outgoing-status)]
    (case status
      :sending  [message-activity-indicator]
      :not-sent [message-not-sent-text chat-id message-id]
      (if (and (not outgoing)
               (:command content))
        [command-status content]
        (when last-outgoing?
          (if (= message-type :group-user-message)
            [group-message-delivery-status message]
            (if outgoing
              [text-status status])))))))

(defview message-author-name [from message-username]
  (letsubs [username [:get-contact-name-by-identity from]]
    [react/text {:style style/message-author-name} (or username
                                                       message-username
                                                       (gfycat/generate-gfy from))])) ; TODO: We defensively generate the name for now, to be revisited when new protocol is defined

(defn message-body
  [{:keys [last-in-group?
           display-photo?
           display-username?
           from
           outgoing
           username] :as message} content]
  [react/view (style/group-message-wrapper message)
   [react/view (style/message-body message)
    (when display-photo?
      [react/view style/message-author
       (when last-in-group?
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-profile from])}
          [react/view
           [photos/member-photo from]]])])
    [react/view (style/group-message-view outgoing)
     (when display-username?
       [message-author-name from username])
     [react/view {:style (style/timestamp-content-wrapper message)}
      content]]]
   [react/view (style/delivery-status outgoing)
    [message-delivery-status message]]])

(defn chat-message [{:keys [outgoing group-chat current-public-key content-type content] :as message}]
  [react/view
   [react/touchable-highlight {:on-press      (fn [_]
                                                (re-frame/dispatch [:set-chat-ui-props {:messages-focused? true}])
                                                (react/dismiss-keyboard!))
                               :on-long-press #(when (= content-type constants/text-content-type)
                                                 (list-selection/share content (i18n/label :t/message)))}
    [react/view {:accessibility-label :chat-item}
     (let [incoming-group (and group-chat (not outgoing))]
       [message-content message-body (merge message
                                            {:current-public-key current-public-key
                                             :group-chat         group-chat
                                             :incoming-group     incoming-group})])]]])
