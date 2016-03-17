(ns messenger.utils.ui-utils
  ;; (:require-macros
  ;;  [natal-shell.components :refer [view text image touchable-highlight list-view
  ;;                                  toolbar-android text-input]]
  ;;  [natal-shell.core :refer [with-error-view]])
  ;; (:require [om.next :as om :refer-macros [defui]])
  )

(set! js/Spinner (.-default (js/require "react-native-loading-spinner-overlay")))

(defn spinner [props]
  (js/React.createElement js/Spinner
                          (clj->js props)))
