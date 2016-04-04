(ns syng-im.components.chat.input.simple-command
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              text
                                              text-input
                                              touchable-highlight]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]
            [reagent.core :as r]))

(defn cancel-command-input []
  (dispatch [:set-input-command nil]))

(defn send-command [chat-id command text]
  (dispatch [:send-chat-command chat-id (:command command) text])
  (cancel-command-input))

(defn simple-command-input-view [command input-options]
  (let [message-atom (r/atom nil)
        chat-id-atom (subscribe [:get-current-chat-id])]
    (fn []
      (let [message @message-atom
            chat-id @chat-id-atom]
        [view {:style {:flexDirection "row"}}
         [view {:style {:flex 1
                        :flexDirection   "column"
                        :backgroundColor "white"}}
          [view {:style {:flexDirection   "column"
                         :margin          10
                         :width           200
                         :backgroundColor "#ebf0f4"
                         :borderRadius    10}}
           [view {:style {:flexDirection "row"}}
            [view {:style {:flexDirection   "column"
                           :margin          10
                           :backgroundColor (:color command)
                           :borderRadius    10}}
             [text {:style {:marginTop -2
                            :marginHorizontal 10
                            :fontSize         14
                            :fontFamily       "Avenir-Roman"
                            :color           "white"}}
              (:text command)]]
            [touchable-highlight {:style {:marginTop   14
                                          :marginRight 16
                                          :position    "absolute"
                                          :top         3
                                          :right       20}
                                  :onPress (fn []
                                             (cancel-command-input))}
             [image {:source res/att
                     :style  {:width  17
                              :height 14}}]]]
           [text-input (merge {:style                 {:flex       1
                                                       :marginLeft 8
                                                       :lineHeight 42
                                                       :fontSize   14
                                                       :fontFamily "Avenir-Roman"
                                                       :color      "black"}
                               :underlineColorAndroid "transparent"
                               :autoFocus             true
                               :keyboardType          "default"
                               :value                 message
                               :onChangeText          (fn [new-text]
                                                        (reset! message-atom new-text))
                               :onSubmitEditing       (fn [e]
                                                        (send-command chat-id command message)
                                                        (reset! message-atom nil))}
                              input-options)]]]
         [touchable-highlight {:style {:marginTop   14
                                       :marginRight 16
                                       :position    "absolute"
                                       :right       20
                                       :bottom      20}
                               :onPress (fn []
                                          (cancel-command-input))}
          [image {:source res/att
                  :style  {:width  34
                           :height 28}}]]]))))
