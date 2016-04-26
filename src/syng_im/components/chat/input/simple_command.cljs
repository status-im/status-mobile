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
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]
            [reagent.core :as r]))

(defn cancel-command-input []
  (dispatch [:set-chat-command nil]))

(defn set-input-message [message]
  (dispatch [:set-chat-command-content message]))

(defn send-command [chat-id command text]
  (dispatch [:stage-command chat-id command text])
  (cancel-command-input))

(defn simple-command-input-view [command input-options]
  (let [chat-id-atom (subscribe [:get-current-chat-id])
        message-atom (subscribe [:get-chat-command-content])]
    (fn [command input-options]
      (let [chat-id @chat-id-atom
            message @message-atom]
        [view {:style {:flexDirection     "row"
                       :height            56
                       :backgroundColor   color-white
                       :elevation         4}}
         [view {:style {:flexDirection   "column"
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
         [text-input (merge {:underlineColorAndroid "transparent"
                             :style                 {:flex       1
                                                     :marginLeft 8
                                                     :marginTop  7
                                                     :fontSize   14
                                                     :fontFamily font
                                                     :color      text1-color}
                             :autoFocus             true
                             :placeholder           "Type"
                             :placeholderTextColor  text2-color
                             :onChangeText          (fn [new-text]
                                                      (set-input-message new-text))
                             :onSubmitEditing       (fn [e]
                                                      (send-command chat-id command message))}
                            input-options)
          message]
         (if (pos? (count message))
           [touchable-highlight {:on-press (fn []
                                             (send-command chat-id command message))
                                 :underlay-color :transparent}
            [view {:style {:marginTop       10
                           :marginRight     10
                           :width           36
                           :height          36
                           :borderRadius    50
                           :backgroundColor color-blue}}
             [image {:source {:uri "icon_send"}
                     :style  {:marginTop   10.5
                              :marginLeft  12
                              :width       15
                              :height      15}}]]]
           [touchable-highlight {:on-press (fn []
                                             (cancel-command-input))
                                 :underlay-color :transparent}
            [view {:style {:marginTop       10
                           :marginRight     10
                           :width           36
                           :height          36}}
             [image {:source res/icon-close-gray
                     :style  {:marginTop   10.5
                              :marginLeft  12
                              :width       12
                              :height      12}}]]])]))))
