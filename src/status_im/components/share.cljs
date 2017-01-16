(ns status-im.components.share
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [platform-specific]]))

(def class (js/require "react-native-share"))

(defn open [opts]
  (.open class (clj->js opts)))

(defn share [text dialog-title]
  (let [list-selection-fn (:list-selection-fn platform-specific)]
    (dispatch [:open-sharing list-selection-fn text dialog-title])))
