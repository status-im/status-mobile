(ns syng-im.components.chat.input.simple-command
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              text
                                              text-input
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font
                                               color-white
                                               color-blue
                                               text1-color
                                               text2-color]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.resources :as res]))

(defn cancel-command-input []
  (dispatch [:cancel-command]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn send-command []
  (dispatch [:stage-command])
  (cancel-command-input))

(defn simple-command-input-view [command input-options]
  (let [message-atom (subscribe [:get-chat-command-content])]
    (fn [command input-options]
      (let [message @message-atom]
        [view {:style {:flexDirection   :row
                       :height          56
                       :backgroundColor color-white
                       :elevation       4}}
         [view {:style {:flexDirection   :column
                        :marginTop       16
                        :marginBottom    16
                        :marginLeft      16
                        :marginRight     0
                        :backgroundColor (:color command)
                        :height          24
                        :borderRadius    50}}
          [text {:style {:marginTop        3
                         :marginHorizontal 12
                         :fontSize         12
                         :fontFamily       font
                         :color            color-white}}
           (:text command)]]
         [text-input (merge {:underlineColorAndroid :transparent
                             :style                 {:flex       1
                                                     :marginLeft 8
                                                     :marginTop  7
                                                     :fontSize   14
                                                     :fontFamily font
                                                     :color      text1-color}
                             :autoFocus             true
                             :placeholder           "Type"
                             :placeholderTextColor  text2-color
                             :onChangeText          set-input-message
                             :onSubmitEditing       send-command}
                            input-options)
          message]
         (if (pos? (count message))
           [touchable-highlight
            {:on-press send-command}
            [view {:style {:marginTop       10
                           :marginRight     10
                           :width           36
                           :height          36
                           :borderRadius    50
                           :backgroundColor color-blue}}
             [image {:source {:uri :icon_send}
                     :style  {:marginTop  10.5
                              :marginLeft 12
                              :width      15
                              :height     15}}]]]
           [touchable-highlight {:on-press cancel-command-input}
            [view {:style {:marginTop   10
                           :marginRight 10
                           :width       36
                           :height      36}}
             [image {:source res/icon-close-gray
                     :style  {:marginTop  10.5
                              :marginLeft 12
                              :width      12
                              :height     12}}]]])]))))
