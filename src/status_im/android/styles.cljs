(ns status-im.android.styles
  (:require [status-im.components.styles :as styles]))

(def components
  {:status-bar {:default     {:height    0
                              :bar-style "default"
                              :color     styles/color-gray}
                :transparent {:height       20
                              :bar-style    "default"
                              :translucent? true
                              :color        styles/color-transparent}}})

(def fonts
  {:default {:font-family "sans-serif"}
   :medium  {:font-family "sans-serif-medium"}})

(def styles
  {:components components
   :fonts      fonts})