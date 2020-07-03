(ns status-im.ui.screens.chat.message.reactions-row
  (:require [status-im.constants :as constants]
            [status-im.ui.screens.chat.message.styles :as styles]
            [quo.react-native :as rn]
            [quo.core :as quo]))

(defn reaction [{:keys [outgoing]} {:keys [own emoji-id quantity]}]
  [rn/view {:style (styles/reaction-style {:outgoing outgoing
                                           :own      own})}
   [rn/image {:source (get constants/reactions emoji-id)
              :style  {:width        16
                       :height       16
                       :margin-right 4}}]
   [quo/text {:style (styles/reaction-quantity-style {:own own})}
    quantity]])

(defn message-reactions [message reactions]
  (when (seq reactions)
    [rn/view {:style (styles/reactions-row message)}
     (for [emoji-reaction reactions]
       ^{:key (str emoji-reaction)}
       [reaction message emoji-reaction])]))
