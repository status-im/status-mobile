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

(defn set-chat-command [message-id command]
  (dispatch [:set-response-chat-command message-id (keyword (:name command))]))

(defn label [command]
  (when command
    (->> (name (:name command))
         (str "request-"))))

(def min-scale 1)
(def max-scale 1.3)

(defn button-animation [val to-value loop? answered?]
  (anim/anim-sequence
    [(anim/anim-delay
       (if (and @loop? (not @answered?))
         request-message-icon-scale-delay
         0))
     (anim/spring val {:toValue to-value})]))

(defn request-button-animation-logic
  [{:keys [to-value val loop? answered?] :as context}]
  (anim/start
    (button-animation val to-value loop? answered?)
    #(if (and @loop? (not @answered?))
      (let [new-value (if (= to-value min-scale) max-scale min-scale)
            context' (assoc context :to-value new-value)]
        (request-button-animation-logic context'))
      (anim/start
        (button-animation val min-scale loop? answered?)))))

(defn request-button [message-id command]
  (let [scale-anim-val (anim/create-value min-scale)
        answered? (subscribe [:is-request-answered? message-id])
        loop? (r/atom true)
        context {:to-value  max-scale
                 :val       scale-anim-val
                 :answered? answered?
                 :loop?     loop?}]
    (r/create-class
      {:component-did-mount
       (when-not @answered? #(request-button-animation-logic context))
       :component-will-unmount
       #(reset! loop? false)
       :reagent-render
       (fn [message-id {command-icon :icon :as command}]
         (if command
           [touchable-highlight
            {:on-press            (when-not @answered?
                                    #(set-chat-command message-id command))
             :style               st/command-request-image-touchable
             :accessibility-label (label command)}
            [animated-view {:style (st/command-request-image-view command scale-anim-val)}
             (if command-icon
               [image {:source {:uri command-icon}
                       :style  st/command-request-image}])]]))})))

(defn message-content-command-request
  [{:keys [message-id content from incoming-group]}]
  (let [commands-atom (subscribe [:get-responses])
        answered? (subscribe [:is-request-answered? message-id])]
    (fn [{:keys [message-id content from incoming-group]}]
      (let [commands @commands-atom
            {:keys [command content]} (parse-command-request commands content)]
        [view st/comand-request-view
         [touchable-highlight
          {:on-press            (when-not @answered?
                                  #(set-chat-command message-id command))}
          [view st/command-request-message-view
           (when incoming-group
             [text {:style st/command-request-from-text
                    :font  :default}
              from])
           [text {:style st/style-message-text
                  :font  :default}
            content]]]
         [request-button message-id command]
         (when (:request-text command)
           [view st/command-request-text-view
            [text {:style st/style-sub-text
                   :font  :default}
             (:request-text command)]])]))))
