(ns syng-im.chat.views.suggestions
  (:require-macros
    [natal-shell.core :refer [with-error-view]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [syng-im.components.react :refer [view
                                              text
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.chat.styles.suggestions :as st]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defn suggestion-list-item
  [{:keys [description command]
    label :text
    :as   suggestion}]
  [touchable-highlight
   {:onPress #(set-command-input (keyword command))}
   [view st/suggestion-item-container
    [view (st/suggestion-background suggestion)
     [text {:style st/suggestion-text} label]]
    [text {:style st/suggestion-description} description]]])

(defn render-row [row _ _]
  (list-item [suggestion-list-item (js->clj row :keywordize-keys true)]))

(defn suggestions-view []
  (let [suggestions-atom (subscribe [:get-suggestions])]
    (fn []
      (let [suggestions @suggestions-atom]
        (when (seq suggestions)
          [view (st/suggestions-container suggestions)
           [list-view {:dataSource          (to-datasource suggestions)
                       :enableEmptySections true
                       :renderRow           render-row
                       :style               {}}]])))))
