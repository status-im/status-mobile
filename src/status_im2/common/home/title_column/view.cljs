(ns status-im2.common.home.title-column.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [status-im2.common.home.title-column.style :as style]
    [status-im2.common.plus-button.view :as plus-button]))

(defn view
  [{:keys [label handler accessibility-label customization-color]}]
  [rn/view style/title-column
   [rn/view {:flex 1}
    [quo/text style/title-column-text
     label]]
   [plus-button/plus-button
    {:on-press            handler
     :accessibility-label accessibility-label
     :customization-color customization-color}]])
