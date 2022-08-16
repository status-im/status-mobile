(ns status-im.theme.core
  (:require [quo.theme :as quo-theme]))

(defn change-theme [theme]
  (quo-theme/set-theme theme))
