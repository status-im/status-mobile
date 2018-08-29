(ns status-im.ui.components.text-input.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.text-input.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.tooltip.views :as tooltip]))

(defn text-input-with-label [{:keys [label content error style height container text] :as props}]
  [react/view
   (when label
     [react/text {:style styles/label}
      label])
   [react/view {:style (merge (styles/input-container height) container)}
    [react/text-input
     (merge
      {:style                  (merge styles/input style)
       :placeholder-text-color colors/gray
       :auto-focus             true
       :value text
       :auto-capitalize        :none}
      (dissoc props :style :height))]
    (when content content)]
   (when error
     [tooltip/tooltip error (styles/error label)])])
