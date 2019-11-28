(ns status-im.utils.debounce
  (:require [re-frame.core :as re-frame]))

(def timeout (atom {}))

(defn debounce [event time]
  (when @timeout (js/clearTimeout @timeout))
  (reset! timeout (js/setTimeout #(re-frame/dispatch event) time)))

(defn clear []
  (when @timeout (js/clearTimeout @timeout)))