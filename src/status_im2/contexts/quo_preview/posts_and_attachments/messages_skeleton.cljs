(ns status-im2.contexts.quo-preview.posts-and-attachments.messages-skeleton
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.ui.screens.chat.components.messages-skeleton :as messages-skeleton]))

(defn preview-messages-skeleton []
  [rn/view  {:background-color (colors/theme-colors
                                colors/white
                                colors/neutral-90)
             :flex             1}
   [messages-skeleton/messages-skeleton]])