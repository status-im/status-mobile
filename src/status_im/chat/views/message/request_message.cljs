(ns status-im.chat.views.message.request-message
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.ui.components.react :refer [view
                                                   animated-view
                                                   text
                                                   image
                                                   icon
                                                   touchable-highlight]]
            [status-im.chat.styles.message.message :as st]
            [status-im.chat.models.commands :as commands]
            [status-im.commands.utils :as commands-utils]
            [status-im.ui.components.animation :as anim]
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
    (anim/spring val {:toValue         to-value
                      :useNativeDriver true})]))

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

(defn request-button [message-id _ on-press-handler]
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
      (if (or (nil? on-press-handler) @answered?) (fn []) #(request-button-animation-logic context))
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
  [{:keys [message-id content outgoing] :as message}]
  (letsubs [command             [:get-command (:request-command-ref content)]
            answered?           [:is-request-answered? message-id]
            status-initialized? [:get :status-module-initialized?]
            network             [:network-name]]
    (let [{:keys        [prefill prefill-bot-db prefillBotDb params preview]
           text-content :text} content
          command           (if (and params command)
                              (merge command {:prefill        prefill
                                              :prefill-bot-db (or prefill-bot-db prefillBotDb)})
                              command)
          request-network   (:network params)
          network-mismatch? (and request-network (not= request-network network))
          on-press-handler  (cond
                              network-mismatch? nil
                              (:execute-immediately? command) #(dispatch [:execute-command-immediately command])
                              (and (not answered?) status-initialized?) #(set-chat-command message-id command))]
      [view st/command-request-view
       [touchable-highlight
        {:on-press on-press-handler}
        [view (st/command-request-message-view outgoing)
         (if (:markup preview)
           [view (commands-utils/generate-hiccup (:markup preview))
            (when network-mismatch?
              [text request-network])]
           [text {:style st/style-message-text
                  :font  :default}
            (or preview text-content (:content content))])]]
       (when (:request-text command)
         [view st/command-request-text-view
          [text {:style st/style-sub-text
                 :font  :default}
           (:request-text command)]])
       [request-button message-id command on-press-handler]])))
