(ns messenger.components.spinner)

(set! js/Spinner (.-default (js/require "react-native-loading-spinner-overlay")))

(defn spinner [props]
  (js/React.createElement js/Spinner
                          (clj->js props)))
