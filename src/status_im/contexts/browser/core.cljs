(ns status-im.contexts.browser.core
  (:require [status-im.contexts.browser.constants :as browser.constants]))

(defn freeze-tab?
  [tab-id focused-tab-id]
  (-> #{(dec focused-tab-id) focused-tab-id (inc focused-tab-id)}
      (contains? tab-id)
      not))

(defn tab-position
  [tab-idx]
  (* tab-idx browser.constants/browser-width))
