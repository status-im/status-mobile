(ns status-im.ui.components.qr-code-viewer.views
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.svg :as svg])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn qr-code [{:keys [size value]}]
  (let [uri (reagent/atom nil)]
    (.toString rn-dependencies/qr-code value #(reset! uri %2))
    (fn []
      (when @uri
        [svg/svgxml {:xml @uri :width size :height size}]))))

(defn qr-code-view [size value]
  (when (and size value)
    [react/view {:style               (styles/qr-code-container size)
                 :accessibility-label :qr-code-image}
     [qr-code {:value value
               :size  size}]]))