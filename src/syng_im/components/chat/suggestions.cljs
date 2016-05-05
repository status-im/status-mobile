(ns syng-im.components.chat.suggestions
  (:require-macros
   [natal-shell.core :refer [with-error-view]])
  (:require [clojure.string :as cstr]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              image
                                              text
                                              touchable-highlight
                                              list-view
                                              list-item]]
            [syng-im.components.styles :refer [font
                                               color-white]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.utils.logging :as log]))

(defn set-command-input [command]
  (dispatch [:set-chat-command command]))

(defn suggestion-list-item [suggestion]
  [touchable-highlight {:onPress (fn []
                                   (set-command-input (keyword (:command suggestion))))
                        :underlay-color :transparent}
   [view {:style {:flexDirection    "row"
                  :marginVertical   1
                  :marginHorizontal 0
                  :height           40
                  :backgroundColor  color-white}}
    [view {:style {:flexDirection   "column"
                   :position        "absolute"
                   :top             10
                   :left            60
                   :backgroundColor (:color suggestion)
                   :borderRadius    10}}
     [text {:style {:marginTop -2
                    :marginHorizontal 10
                    :fontSize         14
                    :fontFamily       font
                    :color            color-white}}
      (:text suggestion)]]
    [text {:style {:flex       1
                   :position   "absolute"
                   :top        7
                   :left       190
                   :lineHeight 18
                   :fontSize   14
                   :fontFamily font
                   :color      "black"}}
     (:description suggestion)]]])

(defn render-row [row section-id row-id]
  (list-item [suggestion-list-item (js->clj row :keywordize-keys true)]))

(defn suggestions-view []
  (let [suggestions-atom (subscribe [:get-suggestions])]
    (fn []
      (let [suggestions @suggestions-atom]
        (when (seq suggestions)
          [view {:style {:flexDirection    "row"
                         :marginVertical   1
                         :marginHorizontal 0
                         :height           (min 105 (* 42 (count suggestions)))
                         :backgroundColor  color-white
                         :borderRadius     5}}
           [list-view {:dataSource (to-datasource suggestions)
                       :renderRow  render-row
                       :style      {}}]])))))
