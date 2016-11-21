(ns status-im.chat.views.command
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                touchable-highlight]]
            [status-im.chat.styles.input :as st]))

(defn cancel-command-input []
  (dispatch [:start-cancel-command]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn send-command []
  (dispatch [:stage-command]))

(defn valid? [message validator]
  (if validator
    (validator message)
    (pos? (count message))))

(defview command-icon [command]
  [icon-width [:get :command-icon-width]]
  [view st/command-container
   [view {:style    (st/command-text-container command)
          :onLayout (fn [event]
                      (let [width (.. event -nativeEvent -layout -width)]
                        (when (not= icon-width width)
                          (dispatch [:set :command-icon-width width]))))}
    [text {:style st/command-text} (str "!" (:name command))]]])

(defn cancel-button []
  [touchable-highlight {:on-press cancel-command-input}
   [view st/cancel-container
    [icon :close_gray st/cancel-icon]]])

