(ns status-im.data-store.browser
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-cofx
 :data-store/all-browsers
 (fn [cofx _]))

(defn save-browser-tx
  "Returns tx function for saving browser"
  [{:keys [browser-id] :as browser}]
  (fn [realm]))

(defn remove-browser-tx
  "Returns tx function for removing browser"
  [browser-id]
  (fn [realm]))
