(ns status-im.theme.core
  (:require [quo.theme :as quo.theme]
            [quo2.theme :as quo2.theme]))

(defn change-theme [theme]
  (quo.theme/set-theme theme)
  (quo2.theme/set-theme theme))
