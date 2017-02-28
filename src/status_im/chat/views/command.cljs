(ns status-im.chat.views.command
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                touchable-highlight]]
            [status-im.chat.constants :as chat-consts]
            [status-im.chat.styles.input :as st]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn valid? [message validator]
  (if validator
    (validator message)
    (pos? (count message))))

(defview command-icon [{:keys [bot] :as command}]
  [icon-width [:get :command-icon-width]]
  [view st/command-container
   [view {:style    (st/command-text-container command)
          :onLayout (fn [event]
                      (let [width (.. event -nativeEvent -layout -width)]
                        (when (not= icon-width width)
                          (dispatch [:set :command-icon-width width]))))}
    (let [[command-char command-name]
          (if bot
            [chat-consts/bot-char bot]
            [chat-consts/command-char (:name command)])]
      [text {:style st/command-text} (str command-char command-name)])]])
