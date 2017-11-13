(ns status-im.ui.components.text-input-with-label.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.utils.platform :refer [ios?]]
            [status-im.ui.components.styles :as common]))

(defnstyle text-input [content-height]
  {:placeholder  ""
   :android      {:height         (or content-height 24)
                  :padding-top    0
                  :padding-bottom 0
                  :padding-left   0
                  :margin-top     26
                  :margin-bottom  4
                  :font-size      16}
   :ios          {:height         (or content-height 26)
                  :margin-top     24
                  :margin-bottom  6
                  :font-size      17
                  :letter-spacing -0.2}})

(defstyle component-container
  {:margin-left  16
   :android      {:min-height 76}
   :ios          {:min-height 78}})

(defnstyle label-animated-text [{:keys [label-top label-font-size]}]
  {:position  :absolute
   :top       label-top
   :font-size label-font-size
   :color     common/color-gray4
   :ios       {:letter-spacing -0.2}})

(defstyle description-text
   {:color   common/color-gray4
    :android {:margin-top     6
              :font-size      12}
    :ios     {:margin-top     4
              :font-size      14
              :letter-spacing -0.2}})

(defstyle error-text
   {:color   common/color-red-2
    :android {:margin-top     6
              :font-size      12}
    :ios     {:margin-top     4
              :font-size      14
              :letter-spacing -0.2}})

(defn underline-blured [error]
  {:background-color (if error common/color-red-2 common/color-light-gray2)
   :align-items      :center})

(defn underline-focused [underline-width underline-height error]
   {:height           underline-height
    :width            underline-width
    :background-color (if error common/color-red-2 common/color-light-blue)})

(def label-top-top (if ios? 6 6))

(def label-top-bottom (if ios? 26 26))

(def label-font-size-top (if ios? 14 12))

(def label-font-size-bottom (if ios? 17 16))

(def underline-max-height (if ios? 1 2))
