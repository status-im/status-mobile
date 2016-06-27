(ns status-im.chat.views.message
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [clojure.string :as s]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                animated-view
                                                touchable-highlight]]
            [status-im.components.animation :as anim]
            [status-im.chat.views.request-message :refer [message-content-command-request]]
            [status-im.chat.styles.message :as st]
            [status-im.models.commands :refer [parse-command-msg-content
                                               parse-command-request]]
            [status-im.resources :as res]
            [status-im.utils.datetime :as time]
            [status-im.constants :refer [text-content-type
                                         content-type-status
                                         content-type-command
                                         content-type-command-request]]))

(defn message-date [timestamp]
  [view {}
   [view st/message-date-container
    [text {:style st/message-date-text} (time/to-short-str timestamp)]]])

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

(defn message-content-command [content preview]
  (let [commands-atom (subscribe [:get-commands-and-responses])]
    (fn [content preview]
      (let [commands @commands-atom
            {:keys [command content]}
            (parse-command-msg-content commands content)]
        [view st/content-command-view
         [view st/command-container
          [view (st/command-view command)
           [text {:style st/command-name}
            (str "!" (:name command))]]]
         ;; todo doesn't reflect design
         (when-let [icon (:icon command)]
           [view st/command-image-view
            [image {:source {:uri icon}
                    :style  st/command-image}]])
         (if preview
           preview
           [text {:style st/command-text}
            content])]))))

(defn set-chat-command [msg-id command]
  (dispatch [:set-response-chat-command msg-id (keyword (:name command))]))

(defn label [{:keys [command]}]
  (->> (when command (name command))
       (str "request-")))

;; todo remove (merging leftover)
#_(defview message-content-command-request
  [{:keys [msg-id content from incoming-group]}]
  [commands [:get-responses]]
  (let [{:keys [command content]} (parse-command-request commands content)]
    [touchable-highlight {:onPress             #(set-chat-command msg-id command)
                          :accessibility-label (label command)}
     [view st/comand-request-view
      [view st/command-request-message-view
       (when incoming-group
         [text {:style st/command-request-from-text} from])
       [text {:style st/style-message-text} content]]
      [view (st/command-request-image-view command)
       [image {:source {:uri (:icon command)}
               :style  st/command-request-image}]]
      (when-let [request-text (:request-text command)]
        [view st/command-request-text-view
         [text {:style st/style-sub-text} request-text]])]]))

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
   [text {:style (st/text-message message)} content]])

(defmethod message-content text-content-type
  [wrapper message]
  [wrapper message [text-message message]])

(defmethod message-content content-type-status
  [_ message]
  [message-content-status message])

(defmethod message-content content-type-command
  [wrapper {:keys [content rendered-preview] :as message}]
  [wrapper message
   [message-view message [message-content-command content rendered-preview]]])

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

(defn message-container-animation-logic [{:keys [to-value val callback]}]
  (fn [_]
    (let [to-value @to-value]
      (when (< 0 to-value)
        (anim/start
          (anim/spring val {:toValue  to-value
                            :friction 4
                            :tension  10})
          (fn [arg]
            (when (.-finished arg)
              (callback))))))))

(defn message-container [message & children]
  (if (:new? message)
    (let [layout-height (r/atom 0)
          anim-value (anim/create-value 1)
          anim-callback #(dispatch [:set-message-shown message])
          context {:to-value layout-height
                   :val      anim-value
                   :callback anim-callback}
          on-update (message-container-animation-logic context)]
      (r/create-class
        {:component-did-mount
         on-update
         :component-did-update
         on-update
         :reagent-render
         (fn [message & children]
           @layout-height
           [animated-view {:style (st/message-container anim-value)}
            (into [view {:onLayout (fn [event]
                                     (let [height (.. event -nativeEvent -layout -height)]
                                       (reset! layout-height height)))}]
                  children)])}))
    (into [view] children)))

(defn chat-message
  [{:keys [outgoing delivery-status timestamp new-day group-chat]
    :as   message}]
  [message-container message
   ;; TODO there is no new-day info in message
   (when new-day
     [message-date timestamp])
   [view
    (let [incoming-group (and group-chat (not outgoing))]
      [message-content
       (if incoming-group
         incoming-group-message-body
         message-body)
       (merge message {:delivery-status (keyword delivery-status)
                       :incoming-group  incoming-group})])]])
