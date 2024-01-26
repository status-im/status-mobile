(ns status-im.contexts.shell.share.wallet.style
  (:require
    [quo.foundations.colors :as colors]))

(defn indicator-wrapper-style
  [active?]
  {:width            8
   :height           8
   :border-radius    4
   :background-color colors/white
   :opacity          (if active? 1.0 0.5)})

(def indicator-list-style
  {:display         :flex
   :flex-direction  :row
   :align-items     :center
   :justify-content :center
   :gap             8})
