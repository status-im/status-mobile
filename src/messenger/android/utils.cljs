(ns messenger.android.utils
  (:require-macros
   [natal-shell.async-storage :refer [get-item set-item]]
   [natal-shell.alert :refer [alert]]
   [natal-shell.toast-android :as toast]))

(defn log [obj]
  (.log js/console obj))

(defn toast [s]
  (toast/show s (toast/long)))
