(ns status-im2.common.plus-button.view
  (:require [quo2.core :as quo]))

(defn plus-button
  [{:keys [on-press accessibility-label customization-color]}]
  [quo/button
   {:type                :primary
    :size                32
    :icon                true
    :accessibility-label (or accessibility-label :plus-button)
    :on-press            on-press
    :customization-color customization-color}
   :i/add])
