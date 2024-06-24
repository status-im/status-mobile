(ns status-im.common.raw-data-block.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.raw-data-block.style :as style]))

(defn view
  [{:keys [data bottom-margin?] :or {bottom-margin? true}}]
  [rn/scroll-view
   {:style                   (style/container bottom-margin?)
    :content-container-style style/content}
   [quo/text
    {:size   :paragraph-2
     :weight :code}
    data]])
