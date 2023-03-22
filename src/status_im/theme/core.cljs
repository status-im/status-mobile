(ns status-im.theme.core
  (:require [quo.theme :as quo.theme]
            [quo2.theme :as quo2.theme]
            [re-frame.core :as re-frame]))

(defn change-theme
  [theme]
  (quo.theme/set-theme theme)
  (quo2.theme/set-theme theme))

(re-frame/reg-fx
 :theme/change-theme
 (fn [theme]
   (change-theme theme)))
