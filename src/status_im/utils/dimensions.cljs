(ns status-im.utils.dimensions
  (:require
    [re-frame.core :as re-frame]
    [status-im.ui.components.react :as react]))

(defn add-event-listener
  []
  (.addEventListener ^js react/dimensions
                     "change"
                     #(re-frame/dispatch-sync [:update-window-dimensions %])))

(defn window
  ([]
   (react/get-dimensions "window"))
  ([m]
   (-> m
       (js->clj :keywordize-keys true)
       :window)))
