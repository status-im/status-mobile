(ns status-im.ui.screens.currency-settings.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :wallet.settings/currency
 :<- [:get-current-account]
 (fn [current-account]
   (or (get-in current-account [:settings :wallet :currency]) :usd)))
