(ns quo2.components.common.unread-grey-dot.view
  (:require [react-native.core :as rn]
            [quo2.components.common.unread-grey-dot.style :as style]))

(defn unread-grey-dot
  [accessibility-label]
  [rn/view
   (cond-> {:style style/unread-grey-dot}
     accessibility-label (assoc :accessibility-label accessibility-label))])
