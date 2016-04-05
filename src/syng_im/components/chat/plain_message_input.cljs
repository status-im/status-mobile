(ns syng-im.components.chat.plain-message-input
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              text-input]]
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
        [view {:style {:flexDirection   "row"
                       :margin          1
                       :height          40
                       :backgroundColor "white"
                       :borderRadius    5}}
         [image {:source res/mic
                 :style  {:marginTop  11
                          :marginLeft 14
                          :width      13
                          :height     20}}]
         [text-input {:underlineColorAndroid "transparent"
                      :style                 {:flex       1
                                              :marginLeft 18
                                              :lineHeight 42
                                              :fontSize   14
                                              :fontFamily "Avenir-Roman"
                                              :color      "#9CBFC0"}
                      :autoFocus             true
                      :placeholder           "Type your message here"
                      :onChangeText          (fn [new-text]
                                               (set-input-message new-text))
                      :onSubmitEditing       (fn [e]
                                               (let [{:keys [group-chat chat-id]} @chat]
                                                 ;; TODO get text from state?
                                                 (if group-chat
                                                   (dispatch [:send-group-chat-msg chat-id
                                                              input-message])
                                                   (dispatch [:send-chat-msg chat-id
                                                              input-message])))
                                               (set-input-message nil))}
          input-message]
         [image {:source res/smile
                 :style  {:marginTop   11
                          :marginRight 12
                          :width       18
                          :height      18}}]
         [image {:source res/att
                 :style  {:marginTop   14
                          :marginRight 16
                          :width       17
                          :height      14}}]]]))))
