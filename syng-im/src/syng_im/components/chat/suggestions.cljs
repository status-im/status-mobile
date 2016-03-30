(ns syng-im.components.chat.suggestions
  (:require-macros
   [natal-shell.core :refer [with-error-view]])
  (:require [clojure.string :as cstr]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view
                                              image
                                              text
                                              list-view
                                              list-item]]
            [syng-im.utils.listview :refer [to-datasource]]
            [syng-im.utils.utils :refer [log toast http-post]]
            [syng-im.utils.logging :as log]))

(defn suggestion-list-item [suggestion]
  [view {:style {:flexDirection    "row"
                 :marginVertical   5
                 :marginHorizontal 10
                 :height           20
                 ;; :backgroundColor "white"
                 }}
   [text {:underlineColorAndroid "#9CBFC0"
          :style                 {:flex       1
                                  :marginLeft 18
                                  :lineHeight 18
                                  :fontSize   14
                                  :fontFamily "Avenir-Roman"
                                  :color      "#9CBFC0"}}
    (:text suggestion)]])

(defn render-row [row section-id row-id]
  (list-item [suggestion-list-item (js->clj row :keywordize-keys true)]))

(defn suggestions-view []
  (let [suggestions (subscribe [:get-suggestions])]
    (fn []
      (when @suggestions
        [view {:style {:flexDirection    "row"
                       :marginVertical   5
                       :marginHorizontal 10
                       :height           120
                       :backgroundColor  "#E5F5F6"
                       :borderRadius     5}}
         [list-view {:dataSource (to-datasource @suggestions)
                     :renderRow  render-row
                     :style      {}}]]))))
