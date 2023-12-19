(ns legacy.status-im.utils.dimensions
  (:require
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]))

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
