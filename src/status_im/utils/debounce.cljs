(ns status-im.utils.debounce
  (:require [re-frame.core :as re-frame]))

(def timeout (atom nil))

(defn debounce-and-dispatch
  "Dispatches event only if there were no calls of this function in period of *time* ms"
  [event time]
  (when @timeout (js/clearTimeout @timeout))
  (reset! timeout (js/setTimeout #(re-frame/dispatch event) time)))

(defn clear []
  (when @timeout (js/clearTimeout @timeout)))

(def chill? (atom false))

(defn dispatch-and-chill
  "Dispateches event and ignores next calls in period of *time* ms"
  [event time]
  (when-not @chill?
    (reset! chill? true)
    (js/setTimeout #(reset! chill? false) time)
    (re-frame/dispatch event)))

