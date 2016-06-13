(ns status-im.chat.views.suggestions
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
  [{:keys [description command]
    label :text
    :as   suggestion}]
  [touchable-highlight
   {:onPress #(set-command-input (keyword command))}
   [view st/suggestion-container
    [view st/suggestion-sub-container
     [view (st/suggestion-background suggestion)
      [text {:style st/suggestion-text} label]]
     [text {:style st/value-text} label]
     [text {:style st/description-text} description]]]])

(defn render-row [row _ _]
  (list-item [suggestion-list-item row]))

(defn suggestions-view []
  (let [suggestions-atom (subscribe [:get-suggestions])]
    (fn []
      (let [suggestions @suggestions-atom]
        (when (seq suggestions)
          [view st/container
           [touchable-highlight {:style   st/drag-down-touchable
                                 :onPress (fn []
                                            ;; TODO hide suggestions?
                                            )}
            [view
             [icon :drag_down st/drag-down-icon]]]
           [view (st/suggestions-container (count suggestions))
            [list-view {:dataSource                (to-datasource suggestions)
                        :enableEmptySections       true
                        :keyboardShouldPersistTaps true
                        :renderRow                 render-row}]]])))))
