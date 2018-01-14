(ns status-im.ui.screens.dev.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub :dev-settings
  (fn [db _]
    (:dev/settings db)))
