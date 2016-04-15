(ns syng-im.components.chat.plain-message-input
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              text-input]]
            [syng-im.components.styles :refer [font
                                               text2-color
                                               color-white]]
            [syng-im.components.chat.suggestions :refer [suggestions-view]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]
            [reagent.core :as r]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn plain-message-input-view []
  (let [chat (subscribe [:get-current-chat])
        input-message-atom (subscribe [:get-chat-input-text])]
    (fn []
      (let [input-message @input-message-atom]
        [view {:style {:flexDirection "column"}}
        [suggestions-view]
        [view {:style {:flexDirection     "row"
                       :height            56
                       :paddingTop        16
                       :paddingHorizontal 16
                       :backgroundColor   color-white
                       :elevation         4}}
         [image {:source res/icon-list
                 :style  {:marginTop    6
                          :marginRight  6
                          :marginBottom 6
                          :marginLeft   5
                          :width        13
                          :height       12}}]
         [text-input {:underlineColorAndroid "transparent"
                      :style                 {:flex       1
                                              :marginLeft 16
                                              :marginTop  -9
                                              :fontSize   14
                                              :fontFamily font
                                              :color      text2-color}
                      :autoFocus             false
                      :placeholder           "Type"
                      :placeholderTextColor  text2-color
                      :onChangeText          (fn [new-text]
                                               (set-input-message new-text))
                      :onSubmitEditing       (fn [e]
                                               (let [{:keys [group-chat chat-id]} @chat]
                                                 (if group-chat
                                                   (dispatch [:send-group-chat-msg chat-id
                                                              input-message])
                                                   (dispatch [:send-chat-msg chat-id
                                                              input-message])))
                                               (set-input-message nil))}
          input-message]
         [image {:source res/icon-smile
                 :style  {:marginTop   2
                          :marginRight 2
                          :width       20
                          :height      20}}]]]))))
