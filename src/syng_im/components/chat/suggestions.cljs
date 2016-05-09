(ns syng-im.components.chat.suggestions
  (:require-macros
   [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              text
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.components.chat.suggestions-styles :as st]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defn suggestion-list-item [suggestion]
  [touchable-highlight
   {:onPress #(set-command-input (keyword (:command suggestion)))}
   [view st/suggestion-item-container
    [view (st/suggestion-background suggestion)
     [text {:style st/suggestion-text}
      (:text suggestion)]]
    [text {:style st/suggestion-description}
     (:description suggestion)]]])

(defn render-row [row _ _]
  (list-item [suggestion-list-item (js->clj row :keywordize-keys true)]))

(defn suggestions-view []
  (let [suggestions-atom (subscribe [:get-suggestions])]
    (fn []
      (let [suggestions @suggestions-atom]
        (when (seq suggestions)
          [view (st/suggestions-container suggestions)
           [list-view {:dataSource (to-datasource suggestions)
                       :enableEmptySections true
                       :renderRow  render-row
                       :style      {}}]])))))
