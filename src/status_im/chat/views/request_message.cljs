(ns status-im.chat.views.request-message
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.chat.styles.message :as st]
            [status-im.models.commands :refer [parse-command-request]]
            [status-im.components.animation :as anim]))

(def request-message-icon-scale-delay 600)

(defn set-chat-command [msg-id command]
  (dispatch [:set-response-chat-command msg-id (:command command)]))

(defn label [{:keys [command]}]
  (->> (name command)
       (str "request-")))

(defn request-button-animation-logic [{:keys [to-value val loop?]}]
  (fn [_]
    (let [loop? @loop?
          minimum 1
          maximum 1.3
          to-scale (if loop?
                     (or @to-value maximum)
                     minimum)]
      (anim/start
        (anim/anim-sequence
          [(anim/anim-delay (if loop? request-message-icon-scale-delay 0))
           (anim/spring val {:toValue to-scale})])
        (fn [arg]
          (when (.-finished arg)
            (dispatch [:set-animation ::request-button-scale-current to-scale])
            (when loop?
              (dispatch [:set-animation ::request-button-scale (if (= to-scale minimum)
                                                                 maximum
                                                                 minimum)]))))))))

(defn request-button [msg-id command]
  (let [to-scale (subscribe [:animations ::request-button-scale])
        cur-scale (subscribe [:animations ::request-button-scale-current])
        scale-anim-val (anim/create-value (or @cur-scale 1))
        loop? (r/atom true)
        context {:to-value to-scale
                 :val      scale-anim-val
                 :loop?    loop?}
        on-update (request-button-animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [msg-id command]
         @to-scale
         [touchable-highlight {:on-press            (fn []
                                                      (reset! loop? false)
                                                      (set-chat-command msg-id command))
                               :style               st/command-request-image-touchable
                               :accessibility-label (label command)}
          [animated-view {:style (st/command-request-image-view command scale-anim-val)}
           [image {:source (:request-icon command)
                   :style  st/command-request-image}]]])})))

(defn message-content-command-request
  [{:keys [msg-id content from incoming-group]}]
  (let [commands-atom (subscribe [:get-commands])]
    (fn [{:keys [msg-id content from incoming-group]}]
      (let [commands @commands-atom
            {:keys [command content]} (parse-command-request commands content)]
        [view st/comand-request-view
         [view st/command-request-message-view
          (when incoming-group
            [text {:style st/command-request-from-text}
             from])
          [text {:style st/style-message-text}
           content]]
         [request-button msg-id command]
         (when (:request-text command)
           [view st/command-request-text-view
            [text {:style st/style-sub-text}
             (:request-text command)]])]))))
