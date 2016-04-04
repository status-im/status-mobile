(ns syng-im.models.user-data
  (:require-macros
   [natal-shell.async-storage :refer [get-item set-item]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.utils :refer [log on-error toast]]))

(defn save-phone-number [phone-number]
  (set-item "user-phone-number" phone-number)
  (dispatch [:set-user-phone-number phone-number]))

(defn load-phone-number []
  (get-item "user-phone-number"
            (fn [error value]
              (if error
                (on-error error)
                (dispatch [:set-user-phone-number (when value
                                                    (str value))])))))
