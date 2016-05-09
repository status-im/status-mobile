(ns syng-im.components.chat.chat-message
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.chat.chat-message-styles :as st]
            [syng-im.models.commands :refer [parse-command-msg-content
                                             parse-command-request]]
            [syng-im.resources :as res]
            [syng-im.constants :refer [text-content-type
                                       content-type-status
                                       content-type-command
                                       content-type-command-request]]))

(defn message-date [{:keys [date]}]
  [view {}
   [view st/message-date-container
    [text {:style st/message-date-text} date]]])

(defn contact-photo [{:keys [photo-path]}]
  [view st/contact-photo-container
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  st/contact-photo}]])

(defn contact-online [{:keys [online]}]
  (when online
    [view st/online-container
     [view st/online-dot-left]
     [view st/online-dot-right]]))

(defn message-content-status [{:keys [from content]}]
  [view st/status-container
   [view st/status-image-view
    [contact-photo {}]
    [contact-online {:online true}]]
   [text {:style st/status-from} from]
   [text {:style st/status-text} content]])

(defn message-content-audio [_]
  [view st/audio-container
   [view st/play-view
    [image {:source res/play
            :style  st/play-image}]]
   [view st/track-container
    [view st/track]
    [view st/track-mark]
    [text {:style st/track-duration-text} "03:39"]]])

(defn message-content-command [content]
  (let [commands-atom (subscribe [:get-commands])]
    (fn [content]
      (let [commands @commands-atom
            {:keys [command content]}
            (parse-command-msg-content commands content)]
        [view st/content-command-view
         [view st/command-container
          [view (st/command-view command)
           [text {:style st/command-name}
            (:text command)]]]
         [image {:source (:icon command)
                 :style  st/command-image}]
         [text {:style st/command-text}
          ;; TODO isn't smart
          (if (= (:command command) :keypair-password)
            "******"
            content)]]))))

(defn set-chat-command [msg-id command]
  (dispatch [:set-response-chat-command msg-id (:command command)]))

(defn message-content-command-request
  [{:keys [msg-id content from incoming-group]}]
  (let [commands-atom (subscribe [:get-commands])]
    (fn [{:keys [msg-id content from incoming-group]}]
      (let [commands @commands-atom
            {:keys [command content]} (parse-command-request commands content)]
        [touchable-highlight {:onPress #(set-chat-command msg-id command)}
         [view st/comand-request-view
          [view st/command-request-message-view
           (when incoming-group
             [text {:style st/command-request-from-text}
              from])
           [text {:style st/style-message-text}
            content]]
          [view (st/command-request-image-view command)
           [image {:source (:request-icon command)
                   :style  st/command-request-image}]]
          (when (:request-text command)
            [view st/command-request-text-view
             [text {:style st/style-sub-text}
              (:request-text command)]])]]))))

(defn message-view
  [message content]
  [view (st/message-view message)
   #_(when incoming-group
       [text {:style message-author-text}
        "Justas"])
   content])

(defmulti message-content (fn [_ message]
                            (message :content-type)))

(defmethod message-content content-type-command-request
  [wrapper message]
  [wrapper message [message-content-command-request message]])

(defn text-message
  [{:keys [content] :as message}]
  [message-view message
   [text {:style (st/text-message message)}
    content]])

(defmethod message-content text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-status
  [_ message]
  [message-content-status message])

(defmethod message-content content-type-command
  [wrapper {:keys [content] :as message}]
  [wrapper message
   [message-view message [message-content-command content]]])

(defmethod message-content :default
  [wrapper {:keys [content-type content] :as message}]
  [wrapper message
   [message-view message
    [message-content-audio {:content      content
                            :content-type content-type}]]])

(defn message-delivery-status [{:keys [delivery-status]}]
  [view st/delivery-view
   [image {:source (case delivery-status
                     :delivered {:uri :icon_ok_small}
                     :seen {:uri :icon_ok_small}
                     :seen-by-everyone {:uri :icon_ok_small}
                     :failed res/delivery-failed-icon)
           :style  st/delivery-image}]
   [text {:style st/delivery-text}
    (case delivery-status
      :delivered "Delivered"
      :seen "Seen"
      :seen-by-everyone "Seen by everyone"
      :failed "Failed")]])

(defn member-photo [{:keys [photo-path]}]
  [view st/photo-view
   [image {:source (if (s/blank? photo-path)
                     res/user-no-photo
                     {:uri photo-path})
           :style  st/photo}]])

(defn incoming-group-message-body
  [{:keys [selected same-author] :as message} content]
  (let [delivery-status :seen-by-everyone]
    [view st/group-message-wrapper
     (when selected
       [text {:style st/selected-message}
        "Mar 7th, 15:22"])
     [view (st/incoming-group-message-body-st message)
      [view st/message-author
       (when (not same-author) [member-photo {}])]
      [view st/group-message-view
       content
       ;; TODO show for last or selected
       (when (and selected delivery-status)
         [message-delivery-status {:delivery-status delivery-status}])]]]))

(defn message-body
  [{:keys [outgoing] :as message} content]
  (let [delivery-status :seen]
    [view (st/message-body message)
     content
     (when (and outgoing delivery-status)
       [message-delivery-status {:delivery-status delivery-status}])]))

(defn chat-message
  [{:keys [outgoing delivery-status date new-day group-chat]
    :as   message}]
  [view {}
   (when new-day [message-date {:date date}])
   [view {}
    (let [incoming-group (and group-chat (not outgoing))]
      [message-content
       (if incoming-group
         incoming-group-message-body
         message-body)
       (merge message {:delivery-status (keyword delivery-status)
                       :incoming-group  incoming-group})])]])
