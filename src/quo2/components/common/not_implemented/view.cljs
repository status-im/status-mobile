(ns quo2.components.common.not-implemented.view 
  (:require [react-native.core :as rn]
            [quo2.components.common.not-implemented.style :as style]))

(defn not-implemented 
  [blur?]
    [rn/text {:style (style/text blur?)}
     "not implemented"])
