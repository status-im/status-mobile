(ns status-im.switcher.utils
  (:require [re-frame.core :as re-frame]))

(def switcher-container-view-id (atom nil))

(re-frame/reg-fx :switcher-container-view-id #(reset! switcher-container-view-id %))
