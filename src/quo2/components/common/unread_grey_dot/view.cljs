(ns quo2.components.common.unread-grey-dot.view
  (:require [react-native.core :as rn]
            [quo2.components.common.unread-grey-dot.style :as style]))

(defn unread-grey-dot
  []
  [rn/view
   {:style style/unread-grey-dot}])
