(ns status-im.common.alert-banner.style
  (:require [status-im.common.alert-banner.constants :as alert-banner.constants]))

(def ^:const alert-banner-height 40)

(defn container
  [background-color]
  {:background-color background-color})

(def second-banner-wrapper
  {:margin-top              (- alert-banner.constants/border-radius)
   :overflow                :hidden
   :border-top-left-radius  alert-banner.constants/border-radius
   :border-top-right-radius alert-banner.constants/border-radius})

(defn hole-view
  [background-color]
  {:padding-top      11
   :align-items      :center
   :height           alert-banner.constants/hole-view-height
   :background-color background-color})
