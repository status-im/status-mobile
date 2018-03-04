(ns status-im.ui.components.qr-code-viewer.views
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.react :as react]))

(defn qr-code [props]
  (reagent/create-element
    rn-dependencies/qr-code
    (clj->js (merge {:inverted true} props))))

(defn- footer [style value]
  [react/view styles/footer
   [react/view styles/wallet-info
    [react/text {:style               (merge styles/hash-value-text style)
                 :accessibility-label :address-text}
     value]]])

(defn qr-code-viewer [{:keys [style hint-style footer-style]} value hint legend]
  {:pre [(not (nil? value))]}
  (let [{:keys [width height]} (react/get-dimensions "window")]
    [react/view {:style (merge styles/qr-code style)}
     [react/text {:style (merge styles/qr-code-hint hint-style)}
      hint]
     (when width
       (let [size (int (* 0.7 (min width height)))]
         [react/view {:style               (styles/qr-code-container size)
                      :accessibility-label :qr-code-image}
          [qr-code {:value value
                    :size  (- size (* 2 styles/qr-code-padding))}]]))
     [footer footer-style legend]]))
