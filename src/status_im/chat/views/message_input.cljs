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
            [reagent.core :as r]
            [clojure.string :as str]))

(defn send-button [{:keys [on-press accessibility-label]}]
  [touchable-highlight {:on-press            on-press
                        :accessibility-label accessibility-label}
   [view st/send-wrapper
    [view st/send-container
     [icon :send st/send-icon]]]])

(defn plain-input-options [disable?]
  {:style             st-message/message-input
   :editable          (not disable?)})

(defn on-press-commands-handler
  [{:keys [suggestions-trigger]}]
  #(dispatch [:send-command!]))

(defn command-input-options [command icon-width disable?]
  {:style             (st-response/command-input icon-width disable?)
   :on-change-text    (when-not disable? command/set-input-message)
   :on-submit-editing (on-press-commands-handler command)})

(defview message-input [input-options set-layout-size]
  [input-message [:get-chat-input-text]
   disable? [:get :disable-input]
   active? [:chat :is-active]
   input-ref (r/atom nil)
   change-content-size? (r/atom true)]
  [text-input (merge
                (plain-input-options (or disable? (not active?)))
                {:placeholder-text-color :#c0c5c9
                 :auto-focus             false
                 :multiline              true
                 :ref                    #(reset! input-ref %)
                 :on-content-size-change #(let [size (-> (.-nativeEvent %)
                                                         (.-contentSize)
                                                         (.-height))]
                                            (when @change-content-size?
                                              (set-layout-size size)))
                 :accessibility-label    :input
                 :on-focus               #(do (dispatch [:set :focused true])
                                              (dispatch [:set-chat-ui-props :show-emoji? false]))
                 :on-blur                #(do (dispatch [:set :focused false])
                                              (set-layout-size 0))
                 :default-value          (or input-message "")
                 :on-key-press           #(when (= "Enter" (.-key (.-nativeEvent %)))
                                            (reset! change-content-size? false))
                 :on-change-text         #(when-not disable?
                                            (if (str/includes? % "\n")
                                              (do
                                                (.clear @input-ref)
                                                (plain-message/set-input-message "")
                                                (plain-message/send)
                                                (reset! change-content-size? true))
                                              (plain-message/set-input-message %)))}
                input-options)])

(defview command-input [input-options {:keys [fullscreen] :as command}]
  [input-command [:get-chat-command-content]
   icon-width [:command-icon-width]
   disable? [:get :disable-input]]
  [text-input (merge
                (command-input-options command icon-width disable?)
                {:auto-focus          (not fullscreen)
                 :blur-on-submit      false
                 :accessibility-label :input
                 :on-focus            #(dispatch [:set :focused true])
                 :on-blur             #(dispatch [:set :focused false])
                 :default-value       (or input-command "")}
                input-options)])

(defn plain-message-get-initial-state [_]
  {:height 0})

(defn plain-message-input-view [_]
  (let [command?             (subscribe [:command?])
        command              (subscribe [:get-chat-command])
        input-command        (subscribe [:get-chat-command-content])
        input-message        (subscribe [:get-chat-input-text])
        valid-plain-message? (subscribe [:valid-plain-message?])
        component            (r/current-component)
        set-layout-size      #(r/set-state component {:height %})]
    (r/create-class
      {:get-initial-state
       plain-message-get-initial-state
       :component-will-update
       (fn [_]
         (when (or (and @command? (str/blank? @input-command))
                   (and (not @command?) (not @input-message)))
           (set-layout-size 0)))
       :reagent-render
       (fn [{:keys [input-options]}]
         (let [{:keys [height]} (r/state component)]
           [view st/input-container
            [view (st/input-view height)
             [plain-message/commands-button height #(set-layout-size 0)]
             [view (st/message-input-container height)
              (if @command?
                [command-input input-options @command]
                [message-input input-options set-layout-size])]
             [plain-message/smile-button height]
             (when (or (and @command? (not (str/blank? @input-command)))
                       @valid-plain-message?)
               (let [on-press (if @command?
                                (on-press-commands-handler @command)
                                plain-message/send)]
                 [send-button {:on-press #(do (dispatch [:set-chat-ui-props :show-emoji? false])
                                              (on-press %))}]))
             (when (and @command? (= :command (:type @command)))
               [command/command-icon @command])]]))})))

