(ns status-im.utils.dimensions
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]))

(declare window)

(defn add-event-listener []
  (.addEventListener react/dimensions
                     "change"
                     #(do
                        (re-frame/dispatch [:update-window-dimensions %]))))

(defn window
  ([]
   (react/get-dimensions "window"))
  ([m]
   (-> m
       (js->clj :keywordize-keys true)
       :window)))

(defn fit-two-pane? []
  (let [width (get (window) :width)]
    (>= width constants/two-pane-min-width)))
