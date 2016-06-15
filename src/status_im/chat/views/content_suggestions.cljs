(ns status-im.chat.views.content-suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.chat.styles.content-suggestions :as st]
            [status-im.utils.listview :refer [to-datasource]]))

(defn set-command-content [content]
  (dispatch [:set-chat-command-content content]))

(defn suggestion-list-item [{:keys [value description]}]
  [touchable-highlight {:onPress #(set-command-content value)}
   [view st/suggestion-container
    [view st/suggestion-sub-container
     [text {:style st/value-text} value]
     [text {:style st/description-text} description]]]])

(defn render-row [row _ _]
  (list-item [suggestion-list-item row]))

(defview content-suggestions-view []
  [suggestions [:get-content-suggestions]]
  (when (seq suggestions)
    [view st/container
     [touchable-highlight {:style   st/drag-down-touchable
                           ;; TODO hide suggestions?
                           :onPress (fn [])}
      [view [icon :drag_down st/drag-down-icon]]]
     suggestions]))
