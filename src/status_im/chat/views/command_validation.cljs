(ns status-im.chat.views.command-validation
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :as c]
            [status-im.chat.styles.command-validation :as st]
            [status-im.utils.listview :as lw]))

(defn message [{:keys [title description]}]
  (c/list-item
    [c/view
     [c/text {:style st/title} title]
     [c/text {:style st/description} description]]))

(defn messages-list [errors]
  [c/list-view {:renderRow                 message
                :keyboardShouldPersistTaps true
                :dataSource                (lw/to-datasource errors)
                :style                     st/messages-container}])

(defview validation-messages []
  [validation-messages [:validation-errors]
   custom-errors [:custom-validation-errors]
   command? [:command?]]
  [c/view
   (cond (seq custom-errors)
         (vec (concat [c/view] custom-errors))

         (seq validation-messages)
         [messages-list validation-messages]

         :else nil)])
