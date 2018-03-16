(ns status-im.ui.components.text-input.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.text-input.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.tooltip.views :as tooltip]))

(defn text-input-with-label [{:keys [label error style height] :as props}]
  [react/view
   (when label
     [react/text {:style styles/label}
      label])
   [react/view {:style (styles/input-container height)}
    [react/text-input
     (merge
       {:style                  (merge styles/input style)
        :placeholder-text-color colors/gray
        :auto-focus             true
        :auto-capitalize        :none}
       (dissoc props :style :height))]]
   (when error
     [tooltip/tooltip error (styles/error label)])])
