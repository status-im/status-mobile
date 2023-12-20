(ns status-im.common.plus-button.view
  (:require
    [quo.core :as quo]))

(defn plus-button
  [{:keys [on-press accessibility-label customization-color]}]
  [quo/button
   {:type                :primary
    :size                32
    :icon-only?          true
    :accessibility-label (or accessibility-label :plus-button)
    :on-press            on-press
    :customization-color customization-color}
   :i/add])
