(ns status-im.android.core
  (:require [status-im.native-module.core :as status]
            [status-im.core :as core]))

(defn init []
  (status/set-soft-input-mode status/adjust-resize)
  (core/init core/root))
