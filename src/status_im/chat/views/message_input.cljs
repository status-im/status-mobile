(ns status-im.chat.views.message-input
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                animated-view
                                                icon
                                                touchable-highlight
                                                text-input]]
            [status-im.chat.views.plain-message :as plain-message]
            [status-im.chat.views.command :as command]
            [status-im.chat.styles.message-input :as st]
            [status-im.chat.styles.plain-message :as st-message]
            [status-im.chat.styles.response :as st-response]
            [status-im.accessibility-ids :as id]
            [reagent.core :as r]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn send-button [{:keys [on-press accessibility-label]}]
  [touchable-highlight {:on-press            on-press
                        :accessibility-label accessibility-label}
   [view st/send-wrapper
    [view st/send-container
     [icon :send st/send-icon]]]])

(defn plain-input-options [disable?]
  {:style             st-message/message-input
   :on-change-text    (when-not disable? plain-message/set-input-message)
   :editable          (not disable?)
   :on-submit-editing plain-message/send})

(defn command-input-options [icon-width disable? sending-disabled?]
  {:style             (st-response/command-input icon-width disable?)
   :on-change-text    (when-not disable? command/set-input-message)
   :on-submit-editing (fn []
                        (when-not sending-disabled?
                          (dispatch [:send-command!])))})

(defn get-options [{:keys [type placeholder]} command-type]
  (let [options (case (keyword type)
                  :phone {:keyboard-type "phone-pad"}
                  :password {:secure-text-entry true}
                  :number {:keyboard-type "numeric"}
                  nil)]
    (if (= :response command-type)
      (if placeholder
        (assoc options :placeholder placeholder)
        options)
      (assoc options :placeholder ""))))

(defview message-input [set-layout-size]
  [input-message [:get-chat-input-text]
   input-command [:get-chat-command-content]
   command [:get-chat-command]
   icon-width [:command-icon-width]
   disable? [:get :disable-input]
   active? [:chat :is-active]
   command? [:command?]
   parameter [:get-command-parameter]
   type [:command-type]
   sending-disabled? [:chat-ui-props :sending-disabled?]]
  [text-input
   (merge
     (if command?
       (command-input-options icon-width disable? sending-disabled?)
       (plain-input-options (or disable? (not active?))))
     {:placeholder-text-color :#c0c5c9
      :auto-focus             (when command?
                                (not (:fullscreen command)))
      :blur-on-submit         false
      :multiline              true
      :on-content-size-change #(let [size (-> (.-nativeEvent %)
                                              (.-contentSize)
                                              (.-height))]
                                 (set-layout-size size))
      :accessibility-label    id/chat-message-input
      :on-focus               #(do (dispatch [:set :focused true])
                                   (dispatch [:set-chat-ui-props :show-emoji? false]))
      :on-blur                #(do (dispatch [:set :focused false])
                                   (set-layout-size 0))
      :default-value          (if command?
                                (or input-command "")
                                (or input-message ""))}
     (when command?
       (get-options parameter type)))])

(defn plain-message-get-initial-state [_]
  {:height 0})

(defn plain-message-input-view []
  (let [command?             (subscribe [:command?])
        command              (subscribe [:get-chat-command])
        input-command        (subscribe [:get-chat-command-content])
        input-message        (subscribe [:get-chat-input-text])
        valid-plain-message? (subscribe [:valid-plain-message?])
        component            (r/current-component)
        set-layout-size      #(r/set-state component {:height %})
        sending-disabled?    (subscribe [:chat-ui-props :sending-disabled?])]
    (r/create-class
      {:get-initial-state
       plain-message-get-initial-state
       :component-will-update
       (fn [_]
         (when (or (and @command? (str/blank? @input-command))
                   (and (not @command?) (not @input-message)))
           (set-layout-size 0)))
       :reagent-render
       (fn []
         (let [{:keys [height]} (r/state component)]
           [view st/input-container
            [view (st/input-view height)
             [plain-message/commands-button height #(set-layout-size 0)]
             [view (st/message-input-container height)
              [message-input set-layout-size]]
             [plain-message/smile-button height]
             (when (or (and @command? (not (str/blank? @input-command)))
                       @valid-plain-message?)
               (let [on-press (if @command?
                                #(dispatch [:send-command!])
                                plain-message/send)]
                 [send-button {:on-press (fn [e]
                                           (when-not @sending-disabled?
                                             (dispatch [:set-chat-ui-props :show-emoji? false])
                                             (on-press e)))
                               :accessibility-label id/chat-send-button}]))
             (when (and @command? (= :command (:type @command)))
               [command/command-icon @command])]]))})))
