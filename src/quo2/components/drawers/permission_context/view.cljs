(ns quo2.components.drawers.permission-context.view
  (:require [react-native.core :as rn]
            [quo2.components.drawers.permission-context.style :as style]))

(defn view
  [children]
  [rn/view {:style style/container}
   children])
