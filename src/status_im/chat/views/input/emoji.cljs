(ns status-im.chat.views.input.emoji
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.ui.components.react :refer [view
                                                text
                                                icon
                                                emoji-picker]]
            [status-im.chat.styles.input.emoji :as style]
            [status-im.i18n :refer [label]]))

(defview emoji-view []
  [keyboard-max-height [:get :keyboard-max-height]]
  [view {:style (style/container keyboard-max-height)}
   [view style/emoji-container
    [emoji-picker {:style           style/emoji-picker
                   :hideClearButton true
                   :onEmojiSelected #(dispatch [:add-to-chat-input-text %])}]]])
