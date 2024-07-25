(ns status-im.common.raw-data-block.view
  (:require [quo.core :as quo]
            [react-native.gesture :as gesture]
            [status-im.common.raw-data-block.style :as style]))

(defn view
  [data]
  [gesture/scroll-view
   {:style style/container}
   [quo/text
    {:size   :paragraph-2
     :style  style/content
     :weight :code}
    data]])
