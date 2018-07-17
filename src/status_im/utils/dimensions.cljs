(ns status-im.utils.dimensions
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]))

(defn add-event-listener []
  (.addEventListener react/dimensions
                     "change"
                     #(re-frame/dispatch [:update-window-dimensions %])))

(defn window
  ([]
   (react/get-dimensions "window"))
  ([m]
   (-> m
       (js->clj :keywordize-keys true)
       :window)))
