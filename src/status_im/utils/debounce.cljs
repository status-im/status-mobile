(ns status-im.utils.debounce
  (:require [re-frame.core :as re-frame]))

(def timeout (atom {}))

(defn clear [event-key]
  (when-let [event-timeout (get @timeout event-key)]
    (js/clearTimeout event-timeout)))

(defn clear-all []
  (doseq [[_ v] @timeout]
    (js/clearTimeout v)))

(defn debounce-and-dispatch
  "Dispatches event only if there were no calls of this function in period of *time* ms"
  [event time]
  (let [event-key (first event)]
    (clear event-key)
    (swap! timeout assoc event-key (js/setTimeout #(re-frame/dispatch event) time))))

(def chill (atom {}))

(defn dispatch-and-chill
  "Dispateches event and ignores next calls in period of *time* ms"
  [event time]
  (let [event-key (first event)]
    (when-not (get @chill event-key)
      (swap! chill assoc event-key true)
      (js/setTimeout #(swap! chill assoc event-key false) time)
      (re-frame/dispatch event))))