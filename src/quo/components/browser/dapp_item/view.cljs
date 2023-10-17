(ns quo.components.browser.dapp-item.view
  (:require [quo.components.browser.dapp-item.style :as style]
            [quo.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn view
  [{:keys [logo name]}]
  [rn/view
   {:style style/root-container}
   [fast-image/fast-image
    {:source (or (:source logo) logo)
     :style  style/logo}]
   [rn/view
    {:style style/gap}]
   [text/text
    {:size            :paragraph-2
     :weight          :medium
     :style           style/text
     :number-of-lines 1}
    (str name)]])
