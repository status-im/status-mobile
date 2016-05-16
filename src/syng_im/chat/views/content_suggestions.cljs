(ns syng-im.chat.views.content-suggestions
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              icon
                                              text
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.chat.styles.content-suggestions :as st]
            [syng-im.utils.listview :refer [to-datasource]]))

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
    [view
     [touchable-highlight {:style   st/drag-down-touchable
                           ;; TODO hide suggestions?
                           :onPress (fn [])}
      [view [icon :drag_down st/drag-down-icon]]]
     [view (st/suggestions-container (count suggestions))
      [list-view {:dataSource (to-datasource suggestions)
                  :renderRow  render-row}]]]))
