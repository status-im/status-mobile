(ns status-im.chat.views.command-validation
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :as c]
            [status-im.chat.styles.command-validation :as st]
            [status-im.utils.listview :as lw]))

(defn error [{:keys [title description]}]
  (c/list-item
    [c/view
     [c/text title]
     [c/text description]]))

(defn errors-list [errors]
  [c/list-view {:renderRow                 error
                :keyboardShouldPersistTaps true
                :dataSource                (lw/to-datasource errors)}])

(defview errors []
  [errors [:validation-errors]
   custom-errors [:custom-validation-errors]
   command? [:command?]]
  (when (and command?
             (or (seq errors)
                 (seq custom-errors)))
    [c/scroll-view {:background-color :red}
     (cond (seq custom-errors)
           (vec (concat [c/view] custom-errors))

           (seq errors)
           [errors-list errors])]))
