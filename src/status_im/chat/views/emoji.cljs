(ns status-im.chat.views.emoji
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                text
                                                icon
                                                emoji-picker]]
            [status-im.chat.styles.emoji :as st]
            [status-im.i18n :refer [label]]))

(defview emoji-view []
  [keyboard-max-height [:get :keyboard-max-height]]
  [view {:style (st/container keyboard-max-height)}
   [view st/emoji-container
    [emoji-picker {:style           st/emoji-picker
                   :hideClearButton true
                   :onEmojiSelected #(dispatch [:add-to-chat-input-text %])}]]])
