(ns legacy.status-im.utils.dimensions
  (:require
    [legacy.status-im.ui.components.react :as react]))

(defn window
  ([]
   (react/get-dimensions "window"))
  ([m]
   (-> m
       (js->clj :keywordize-keys true)
       :window)))
