(ns syng-im.components.chat.content-suggestions
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
            [syng-im.components.chat.content-suggestions-styles :as st]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.utils.logging :as log]))

(defn set-command-content [content]
  (dispatch [:set-chat-command-content content]))

(defn suggestion-list-item [suggestion]
  [touchable-highlight {:onPress (fn []
                                   (set-command-content (:value suggestion)))
                        :underlay-color :transparent}
   [view (merge st/suggestion-container
                (when (= (:description suggestion) "Number format 12")
                  {:backgroundColor "blue"}))
    [view st/suggestion-sub-container
     [text {:style st/value-text}
      (:value suggestion)]
     [text {:style st/description-text}
      (:description suggestion)]]]])

(defn render-row [row section-id row-id]
  (list-item [suggestion-list-item (js->clj row :keywordize-keys true)]))

(defn content-suggestions-view []
  (let [suggestions-atom (subscribe [:get-content-suggestions])]
    (fn []
      (let [suggestions @suggestions-atom]
        (when (seq suggestions)
          [view nil
           [touchable-highlight {:style          st/drag-down-touchable
                                 :onPress        (fn []
                                                   ;; TODO hide suggestions?
                                                   )
                                 :underlay-color :transparent}
            [image {:source {:uri "icon_drag_down"}
                    :style  st/drag-down-icon}]]
           [view (st/suggestions-container (count suggestions))
            [list-view {:dataSource (to-datasource suggestions)
                        :renderRow  render-row}]]])))))
