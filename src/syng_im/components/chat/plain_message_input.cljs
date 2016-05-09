(ns syng-im.components.chat.plain-message-input
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [android?
                                              view
                                              image
                                              touchable-highlight
                                              text-input]]
            [syng-im.components.styles :refer [font
                                               text2-color
                                               color-white
                                               color-blue]]
            [syng-im.components.chat.suggestions :refer [suggestions-view]]
            [syng-im.utils.utils :refer [log toast http-post]]))

(defn set-input-message [message]
  (dispatch [:set-chat-input-text message]))

(defn send [chat input-message]
  (let [{:keys [group-chat chat-id]} chat]
    (if group-chat
      ;; todo how much are different both events? is there real reason
      ;; for differentiation here?
      (dispatch [:send-group-chat-msg chat-id input-message])
      (dispatch [:send-chat-msg]))))

(defn plain-message-input-view []
  (let [chat                 (subscribe [:get-current-chat])
        input-message-atom   (subscribe [:get-chat-input-text])
        staged-commands-atom (subscribe [:get-chat-staged-commands])]
    (fn []
      (let [input-message @input-message-atom]
        [view {:style {:flexDirection :column}}
         [suggestions-view]
         [view {:style {:flexDirection   :row
                        :height          56
                        :backgroundColor color-white}}
          [image {:source {:uri :icon_list}
                  :style  {:marginTop    22
                           :marginRight  6
                           :marginBottom 6
                           :marginLeft   21
                           :width        13
                           :height       12}}]
          [text-input {:underlineColorAndroid :transparent
                       :style                 {:flex       1
                                               :marginLeft 16
                                               :marginTop  -2
                                               :padding    0
                                               :fontSize   14
                                               :fontFamily font
                                               :color      text2-color}
                       :autoFocus             (pos? (count @staged-commands-atom))
                       :placeholder           "Type"
                       :placeholderTextColor  text2-color
                       :onChangeText          set-input-message
                       :onSubmitEditing       #(send @chat input-message)}
           input-message]
          [image {:source {:uri :icon_smile}
                  :style  {:marginTop   18
                           :marginRight 18
                           :width       20
                           :height      20}}]
          (when (or (pos? (count input-message))
                    (pos? (count @staged-commands-atom)))
            [touchable-highlight {:on-press       #(send @chat input-message)
                                  :underlay-color :transparent}
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
                               :height     15}}]]])]]))))
