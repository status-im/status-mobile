(ns utils.debounce
  (:require [goog.functions]
            [re-frame.core :as re-frame]))

(def timeout (atom {}))

(defn clear
  [event-key]
  (when-let [event-timeout (get @timeout event-key)]
    (js/clearTimeout event-timeout)))

(defn clear-all
  []
  (doseq [[_ v] @timeout]
    (js/clearTimeout v)))

(defn debounce-and-dispatch
  "Dispatches `event` iff it was not dispatched for the duration of `duration-ms`."
  [event duration-ms]
  (let [event-key (first event)]
    (clear event-key)
    (swap! timeout assoc event-key (js/setTimeout #(re-frame/dispatch event) duration-ms))))

(def chill (atom {}))

(defn dispatch-and-chill
  "Dispatches event and ignores subsequent calls for the duration of `duration-ms`."
  [event duration-ms]
  (let [event-key (first event)]
    (when-not (get @chill event-key)
      (swap! chill assoc event-key true)
      (js/setTimeout #(swap! chill assoc event-key false) duration-ms)
      (re-frame/dispatch event))))

(defn debounce
  [f duration-ms]
  (goog.functions/debounce f duration-ms))
