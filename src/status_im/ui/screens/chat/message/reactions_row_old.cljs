(ns status-im.ui.screens.chat.message.reactions-row-old
  (:require [status-im.constants :as constants]
            [status-im.ui.screens.chat.message.styles :as styles]
            [quo.react-native :as rn]
            [quo.core :as quo]))

(defn reaction [{:keys [outgoing]} {:keys [own emoji-id quantity]} timeline]
  [rn/view {:style (styles/reaction-style {:outgoing (and outgoing (not timeline))
                                           :own      own})}
   [rn/image {:source (get constants/reactions-old emoji-id)
              :style  {:width        16
                       :height       16
                       :margin-right 4}}]
   [quo/text {:accessibility-label (str "emoji-" emoji-id "-is-own-" own)
              :style               (styles/reaction-quantity-style {:own own})}
    quantity]])

(defn message-reactions [message reactions timeline]
  (when (seq reactions)
    [rn/view {:style (styles/reactions-row-old message timeline)}
     (for [emoji-reaction reactions]
       ^{:key (str emoji-reaction)}
       [reaction message emoji-reaction timeline])]))
