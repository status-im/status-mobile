(ns status-im2.common.qr-code-viewer.view
  (:require ["qrcode" :as qr-code-js]
            [cljs-bean.core :as bean]
            [reagent.core :as reagent]
            [status-im2.common.qr-code-viewer.style :as style]
            [react-native.core :as rn]
            [react-native.svg :as svg]))

(defn qr-code
  [{:keys [size value]}]
  (let [uri (reagent/atom nil)]
    (.toString
     qr-code-js
     value
     (bean/->js {:margin 0 :width size})
     #(reset! uri %2))
    (fn []
      (when @uri
        [svg/svgxml {:xml @uri :width size :height size}]))))

(defn qr-code-view
  "Qr Code view including the frame.
  Note: `size` includes frame with `style/qr-code-padding.`"
  [size value]
  (when (and size value)
    [rn/view
     {:style               (style/qr-code-container size)
      :accessibility-label :qr-code-image}
     [qr-code
      {:value value
       :size  (- size (* style/qr-code-padding 2))}]]))
