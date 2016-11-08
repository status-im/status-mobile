(ns status-im.chat.views.message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.i18n :refer [message-status-label]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                animated-view
                                                touchable-highlight
                                                get-dimensions]]
            [status-im.components.animation :as anim]
            [status-im.chat.views.request-message :refer [message-content-command-request]]
            [status-im.chat.styles.message :as st]
            [status-im.chat.styles.command-pill :as pill-st]
            [status-im.chat.views.datemark :refer [chat-datemark]]
            [status-im.models.commands :refer [parse-command-message-content
                                               parse-command-request]]
            [status-im.resources :as res]
            [status-im.utils.datetime :as time]
            [status-im.constants :refer [console-chat-id
                                         wallet-chat-id
                                         text-content-type
                                         content-type-status
                                         content-type-command
                                         content-type-command-request]]
            [status-im.components.chat-icon.screen :refer [chat-icon-message-status]]
            [status-im.utils.identicon :refer [identicon]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.i18n :refer [label]]
            [status-im.chat.utils :as cu]
            [clojure.string :as str]
            [status-im.chat.handlers.console :as console]
            [taoensso.timbre :as log]))

(def window-width (:width (get-dimensions "window")))

(defn message-content-status [_]
  (let [{:keys [chat-id group-chat name color]} (subscribe [:chat-properties [:chat-id :group-chat :name :color]])
        members (subscribe [:current-chat-contacts])]
    (fn [{:keys [messages-count content datemark]}]
      (let [{:keys [photo-path
                    status
                    last-online]} (if @group-chat
                                    {:photo-path  nil
                                     :status      nil
                                     :last-online 0}
                                    (first @members))]
        [view st/status-container
         [chat-icon-message-status @chat-id @group-chat @name @color false]
         [text {:style           st/status-from
                :font            :default
                :number-of-lines 1}
          (if (str/blank? @name)
            (generate-gfy)
            (or @name (label :t/chat-name)))]
         (when (or status content)
           [text {:style st/status-text
                  :font  :default}
            (or status content)])
         (if (> messages-count 1)
           [view st/message-datemark
            [chat-datemark datemark]]
           [view st/message-empty-spacing])]))))

(defn message-content-audio [_]
  [view st/audio-container
   [view st/play-view
    [image {:source res/play
            :style  st/play-image}]]
   [view st/track-container
    [view st/track]
    [view st/track-mark]
    [text {:style st/track-duration-text
           :font  :default}
     "03:39"]]])

(defmulti command-preview (fn [{:keys [command]}] command))

(defmethod command-preview "send"
  [{{:keys [name]} :contact-chat
    :keys          [contact-address params outgoing? current-chat-id]}]
  (let [amount (if (= 1 (count params))
                 (first (vals params))
                 (str params))]
    [text {:style st/command-text
           :font  :default}
     (if (= current-chat-id wallet-chat-id)
       (let [label-val (if outgoing? :t/chat-send-eth-to :t/chat-send-eth-from)]
         (label label-val {:amount    amount
                           :chat-name (or name contact-address)}))
       (label :t/chat-send-eth {:amount amount}))]))

(defmethod command-preview :default
  [{:keys [params preview]}]
  (if preview
    preview
    [text {:style st/command-text
           :font  :default}
     (if (= 1 (count params))
       (first (vals params))
       (str params))]))

(defview message-content-command [{:keys [content rendered-preview chat-id to from outgoing] :as message}]
  [commands [:get-commands-and-responses (if outgoing to from)]
   current-chat-id [:get-current-chat-id]
   contact-chat [:get-in [:chats (if outgoing to from)]]]
  (let [{:keys [command params]} (parse-command-message-content commands content)
        {:keys     [name type]
         icon-path :icon} command]
    [view st/content-command-view
     [view st/command-container
      [view (pill-st/pill command)
       [text {:style pill-st/pill-text
              :font  :default}
        (str (if (= :command type) "!" "?") name)]]]
     (when icon-path
       [view st/command-image-view
        [icon icon-path st/command-image]])
     [command-preview {:command         (:name command)
                       :params          params
                       :outgoing?       outgoing
                       :preview         rendered-preview
                       :contact-chat    contact-chat
                       :contact-address (if outgoing to from)
                       :current-chat-id current-chat-id}]]))

(defn set-chat-command [message-id command]
  (dispatch [:set-response-chat-command message-id (keyword (:name command))]))

(defn message-view
  [message content]
  [view (st/message-view message)
   #_(when incoming-group
       [text {:style message-author-text}
        "Justas"])
   content])

(defmulti message-content (fn [_ message _]
                            (message :content-type)))

(defmethod message-content content-type-command-request
  [wrapper message]
  [wrapper message [message-content-command-request message]])

;; todo rewrite this, naive implementation
(defn- parse-text [string]
  (if (string? string)
    (let [regx         #"\*[^*]+\*"
          general-text (s/split string regx)
          bold-text    (vec (map-indexed
                              (fn [idx string]
                                [text
                                 {:key   (str idx "_" string)
                                  :style {:font-weight :bold}}
                                 (subs string 1 (- (count string) 1))])
                              (re-seq regx string)))
          bold-text'   (if (> (count general-text)
                              (count bold-text))
                         (conj bold-text nil)
                         bold-text)]
      (mapcat vector general-text bold-text'))
    (str string)))

(defn text-message
  [{:keys [content] :as message}]
  [message-view message
   [text {:style (st/text-message message)
          :font  :default}
    (parse-text content)]])

(defmethod message-content text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-status
  [_ message]
  [message-content-status message])

(defmethod message-content content-type-command
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
  [app-db-message-user-statuses [:get-in [:message-user-statuses message-id]]
   app-db-message-status-value [:get-in [:message-statuses message-id :status]]
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
       [image {:source (case status
                         :seen {:uri :icon_ok_small}
                         :failed res/delivery-failed-icon
                         nil)
               :style  st/delivery-image}]
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
  [app-db-message-status-value [:get-in [:message-statuses message-id :status]]]
  (let [delivery-status (get-in user-statuses [chat-id :status])
        command-name    (keyword (:command content))
        status          (cond (and (not (console/commands-with-delivery-status command-name))
                                   (cu/console? chat-id))
                              :seen

                              (cu/wallet? chat-id)
                              :sent

                              :else
                              (or delivery-status message-status app-db-message-status-value :sending))]
    [view st/delivery-view
     [image {:source (case status
                       :seen {:uri :icon_ok_small}
                       :failed res/delivery-failed-icon
                       nil)
             :style  st/delivery-image}]
     [text {:style st/delivery-text
            :font  :default}
      (message-status-label status)]]))

(defview member-photo [from]
  [photo-path [:photo-path from]]
  [view st/photo-view
   [image {:source {:uri (if (s/blank? photo-path)
                           (identicon from)
                           photo-path)}
           :style  st/photo}]])

(defn incoming-group-message-body
  [{:keys [selected same-author from index] :as message} content]
  (let [delivery-status :seen-by-everyone]
    [view st/group-message-wrapper
     (when selected
       [text {:style st/selected-message
              :font  :default}
        "Mar 7th, 15:22"])
     [view (st/incoming-group-message-body-st message)
      [view st/message-author
       (when (or (= index 1)
                 (not same-author)) [member-photo from])]
      [view st/group-message-view
       content
       ;; TODO show for last or selected
       (when (and selected delivery-status)
         [message-delivery-status message])]]]))

(defn message-body
  [{:keys [outgoing message-type] :as message} content]
  [view (st/message-body message)
   content
   (when outgoing
     (if (= (keyword message-type) :group-user-message)
       [group-message-delivery-status message]
       [message-delivery-status message]))])

(defn message-container-animation-logic [{:keys [to-value val callback]}]
  (fn [_]
    (let [to-value @to-value]
      (when (< 0 to-value)
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
         :reagent-render
         (fn [message & children]
           @layout-height
           [animated-view {:style (st/message-animated-container anim-value)}
            (into [view {:style (st/message-container window-width)
                         :onLayout (fn [event]
                                     (let [height (.. event -nativeEvent -layout -height)]
                                       (reset! layout-height height)))}]
                  children)])}))
    (into [view] children)))

(defn chat-message [{:keys [outgoing message-id chat-id user-statuses from]}]
  (let [my-identity (subscribe [:get :current-public-key])
        status      (subscribe [:get-in [:message-user-statuses message-id my-identity]])]
    (r/create-class
      {:component-did-mount
       (fn []
         (when (and (not outgoing)
                    (not= :seen (keyword @status))
                    (not= :seen (keyword (get-in user-statuses [@my-identity :status]))))
           (dispatch [:send-seen! {:chat-id    chat-id
                                   :from       from
                                   :message-id message-id}])))
       :reagent-render
       (fn [{:keys [outgoing group-chat] :as message}]
         [message-container message
          [view
           (let [incoming-group (and group-chat (not outgoing))]
             [message-content
              (if incoming-group
                incoming-group-message-body
                message-body)
              (merge message {:incoming-group incoming-group})])]])})))
