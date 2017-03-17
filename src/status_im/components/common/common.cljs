(ns status-im.components.common.common
  (:require [status-im.components.react :refer [view linear-gradient]]
            [status-im.components.common.styles :as st]))

(defn top-shaddow []
  [linear-gradient
   {:style  st/gradient-bottom
    :colors st/gradient-bottom-colors}])

(defn bottom-shaddow []
  [linear-gradient
   {:style  st/gradient-top
    :colors st/gradient-top-colors}])

(defn separator [style]
  [view st/separator-wrapper
   [view (merge st/separator style)]])
