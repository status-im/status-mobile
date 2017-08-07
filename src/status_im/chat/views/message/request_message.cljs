(ns status-im.chat.views.message.request-message
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                text
                                                image
                                                icon
                                                touchable-highlight]]
            [status-im.chat.styles.message.message :as st]
            [status-im.chat.models.commands :as commands]
            [status-im.components.animation :as anim]
            [taoensso.timbre :as log]))

(def request-message-icon-scale-delay 600)

(defn set-chat-command [message-id command]
  (let [metadata {:to-message-id message-id}]
    (dispatch [:select-chat-input-command command metadata])))

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
             context'  (assoc context :to-value new-value)]
         (request-button-animation-logic context'))
       (anim/start
         (button-animation val min-scale loop? answered?)))))

(defn request-button-label
  "The request button label will be in the form of `request-the-command-name`"
  [command-name]
  (keyword (str "request-" (name command-name))))

(defn request-button [message-id _ _]
  (let [scale-anim-val (anim/create-value min-scale)
        answered?      (subscribe [:is-request-answered? message-id])
        loop?          (r/atom true)
        context        {:to-value  max-scale
                        :val       scale-anim-val
                        :answered? answered?
                        :loop?     loop?}]
    (r/create-class
      {:display-name "request-button"
       :component-did-mount
       (if @answered? (fn []) #(request-button-animation-logic context))
       :component-will-unmount
       #(reset! loop? false)
       :reagent-render
       (fn [message-id {command-icon :icon :as command} on-press-handler]
         (when command
           [touchable-highlight
            {:on-press            on-press-handler
             :style               (st/command-request-image-touchable)
             :accessibility-label (request-button-label (:name command))}
            [animated-view {:style (st/command-request-image-view command scale-anim-val)}
             (when command-icon
               [icon command-icon st/command-request-image])]]))})))

(defview message-content-command-request
  [{:keys [message-id chat-id]}]
  (letsubs [requests [:chat-actions :possible-requests]
            commands [:chat-actions :possible-commands]
            answered? [:is-request-answered? message-id]
            status-initialized? [:get :status-module-initialized?]
            markup [:get-message-preview message-id]]
    (fn [{:keys [message-id content from incoming-group] :as message}]
      (let [{:keys        [prefill prefill-bot-db prefillBotDb params]
             text-content :text} content
            {:keys [command content]} (commands/set-command-for-request message requests commands)
            command          (if (and params command)
                               (merge command {:prefill        prefill
                                               :prefill-bot-db (or prefill-bot-db prefillBotDb)})
                               command)
            on-press-handler (if (:execute-immediately? command)
                               #(dispatch [:execute-command-immediately command])
                               (when (and (not answered?) status-initialized?)
                                 #(set-chat-command message-id command)))]
        [view st/comand-request-view
         [touchable-highlight
          {:on-press on-press-handler}
          [view st/command-request-message-view
           (if (and markup
                    (not (string? markup)))
             [view markup]
             [text {:style st/style-message-text
                    :font  :default}
              (or text-content markup content)])]]
         (when (:request-text command)
           [view st/command-request-text-view
            [text {:style st/style-sub-text
                   :font  :default}
             (:request-text command)]])
         [request-button message-id command on-press-handler]]))))
