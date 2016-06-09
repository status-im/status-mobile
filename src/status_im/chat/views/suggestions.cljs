(ns status-im.chat.views.suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                icon
                                                touchable-highlight
                                                list-view
                                                list-item]]
            [status-im.utils.listview :refer [to-datasource]]
            [status-im.chat.styles.suggestions :as st]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defn suggestion-list-item
  [[command {:keys [description]
              name  :name
              :as   suggestion}]]
  (let [label (str "!" name)]
    [touchable-highlight
     {:onPress #(set-command-input command)}
     [view st/suggestion-container
      [view st/suggestion-sub-container
       [view (st/suggestion-background suggestion)
        [text {:style st/suggestion-text} label]]
       [text {:style st/value-text} label]
       [text {:style st/description-text} description]]]]))

(defn render-row [row _ _]
  (list-item [suggestion-list-item row]))

(defview suggestions-view []
  [suggestions [:get-suggestions]]
  (when (seq suggestions)
    [view
     [touchable-highlight {:style   st/drag-down-touchable
                           :onPress (fn []
                                      ;; TODO hide suggestions?
                                      )}
      [view
       [icon :drag_down st/drag-down-icon]]]
     [view (st/suggestions-container (count suggestions))
      [list-view {:dataSource          (to-datasource suggestions)
                  :enableEmptySections true
                  :renderRow           render-row}]]]))
