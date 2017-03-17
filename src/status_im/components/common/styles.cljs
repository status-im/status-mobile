(ns status-im.components.common.styles
  (:require [status-im.utils.platform :as p]
            [status-im.components.styles :refer [color-white color-light-gray]]))

(def gradient-top
  {:flexDirection   :row
   :height          3
   :backgroundColor color-light-gray})

(def gradient-top-colors
  ["rgba(24, 52, 76, 0.165)"
   "rgba(24, 52, 76, 0.03)"
   "rgba(24, 52, 76, 0.01)"])

(def gradient-bottom
  {:flexDirection   :row
   :height          2
   :backgroundColor color-light-gray})

(def gradient-bottom-colors
  ["rgba(24, 52, 76, 0.01)"
   "rgba(24, 52, 76, 0.05)"])

(def separator-wrapper
  {:background-color color-white})

(def separator
  (get-in p/platform-specific [:component-styles :separator]))
