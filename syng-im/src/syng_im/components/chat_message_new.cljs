(ns syng-im.components.chat-message-new
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              text-input]]
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]
            [reagent.core :as r]))


(defn chat-message-new []
  (let [text    (r/atom nil)
        chat-id (subscribe [:get-current-chat-id])]
    (fn []
      [view {:style {:flexDirection   "row"
                     :margin          10
                     :height          40
                     :backgroundColor "#E5F5F6"
                     :borderRadius    5}}
       [image {:source res/mic
               :style  {:marginTop  11
                        :marginLeft 14
                        :width      13
                        :height     20}}]
       [text-input {:underlineColorAndroid "#9CBFC0"
                    :style                 {:flex       1
                                            :marginLeft 18
                                            :lineHeight 42
                                            :fontSize   14
                                            :fontFamily "Avenir-Roman"
                                            :color      "#9CBFC0"}
                    :autoFocus             true
                    :placeholder           "Enter your message here"
                    :value                 @text
                    :onChangeText          (fn [new-text]
                                             (reset! text new-text)
                                             (r/flush))
                    :onSubmitEditing       (fn [e]
                                             (dispatch [:send-chat-msg @chat-id @text])
                                             (reset! text nil))}]
       [image {:source res/smile
               :style  {:marginTop   11
                        :marginRight 12
                        :width       18
                        :height      18}}]
       [image {:source res/att
               :style  {:marginTop   14
                        :marginRight 16
                        :width       17
                        :height      14}}]])))
