(ns status-im.chat.views.response-suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.chat.styles.response-suggestions :as st]
            [status-im.utils.listview :refer [to-datasource]]))

(defn set-command-content [content]
  (dispatch [:set-chat-command-content content]))

(defn header-list-item [{:keys [header]}]
  [view st/header-container
   [text {:style st/header-text} header]])

(defn suggestion-list-item [{:keys [value description]}]
  [touchable-highlight {:onPress #(set-command-content value)}
   [view st/suggestion-container
    [view st/suggestion-sub-container
     [text {:style st/value-text} value]
     [text {:style st/description-text} description]]]])

(defn render-row [row _ _]
  (list-item (if (:header row)
               [header-list-item row]
               [suggestion-list-item row])))

(defview response-suggestions-view []
  [suggestions [:get-content-suggestions]]
  (when (seq suggestions)
    [view nil
     [view (st/suggestions-container suggestions)
      [list-view {:dataSource                (to-datasource suggestions)
                  :keyboardShouldPersistTaps true
                  :renderRow                 render-row}]]]))
