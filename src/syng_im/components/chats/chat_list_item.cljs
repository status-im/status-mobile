(ns syng-im.components.chats.chat-list-item
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              text
                                              image
                                              touchable-highlight]]
            [syng-im.components.styles :refer [font]]
            [syng-im.utils.logging :as log]
            [syng-im.resources :as res]))

(defn chat-list-item [chat-obj navigator]
  [touchable-highlight {:on-press (fn []
                                    (dispatch [:show-chat (aget chat-obj "chat-id") navigator]))}
   [view {:style {:flexDirection  "row"
                  :width          260
                  :marginVertical 5}}
    [image {:source res/chat-icon
            :style  {:borderWidth 2
                     :borderColor "#FFFFFF"
                     :width       32
                     :height      30
                     :marginRight 5
                     :marginLeft  5}}]
    [text {:style {:fontSize   14
                   :fontFamily font
                   :color      "#4A5258"}}
     (subs (aget chat-obj "name") 0 30)]]])
