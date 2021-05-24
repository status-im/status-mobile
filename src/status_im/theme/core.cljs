(ns status-im.theme.core
  (:require [status-im.ui.components.colors :as colors]
            [quo.theme :as quo-theme]))

(defn change-theme [theme]
  (quo-theme/set-theme theme)
  (colors/set-theme theme))
