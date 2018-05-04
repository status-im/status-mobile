(ns status-im.ui.components.text
  (:require [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- selectable-text [{:keys [value style]}]
    (if platform/ios?
      [react/text-input {:value     value
                         :editable  false
                         :multiline true
                         :style     style}]
      [react/text {:style               style
                   :accessibility-label :address-text
                   :selectable          true}
       value]))
