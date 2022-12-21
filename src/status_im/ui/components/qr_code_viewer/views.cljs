(ns status-im.ui.components.qr-code-viewer.views
  (:require [cljs-bean.core :as bean]
            [reagent.core :as reagent]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.react :as react]
            ["qrcode" :as qr-code-js]
            ["react-native-svg" :refer (SvgXml)]))

(def svgxml (reagent/adapt-react-class SvgXml))

(defn qr-code [{:keys [size value]}]
  (let [uri (reagent/atom nil)]
    (.toString
     qr-code-js
     value
     (bean/->js {:margin 0 :width size})
     #(reset! uri %2))
    (fn []
      (when @uri
        [svgxml {:xml @uri :width size :height size}]))))

(defn qr-code-view
  "Qr Code view including the frame.
  Note: `size` includes frame with `styles/qr-code-padding.`"
  [size value]
  (when (and size value)
    [react/view {:style               (styles/qr-code-container size)
                 :accessibility-label :qr-code-image}
     [qr-code {:value value
               :size  (- size (* styles/qr-code-padding 2))}]]))
