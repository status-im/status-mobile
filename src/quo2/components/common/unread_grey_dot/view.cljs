(ns quo2.components.common.unread-grey-dot.view
  (:require
    [quo2.components.common.unread-grey-dot.style :as style]
    [react-native.core :as rn]))

(defn unread-grey-dot
  [accessibility-label]
  [rn/view
   (cond-> {:style style/unread-grey-dot}
     accessibility-label (assoc :accessibility-label accessibility-label :accessible true))])
