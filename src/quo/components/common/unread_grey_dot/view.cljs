(ns quo.components.common.unread-grey-dot.view
  (:require
    [quo.components.common.unread-grey-dot.style :as style]
    [quo.theme]
    [react-native.core :as rn]))

(defn unread-grey-dot
  [accessibility-label]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     (cond-> {:style (style/unread-grey-dot theme)}
       accessibility-label (assoc :accessibility-label accessibility-label :accessible true))]))
