(ns status-im.ui.components.qr-code-viewer.views
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.qr-code-viewer.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defn qr-code [props]
  (reagent/create-element
   rn-dependencies/qr-code
   (clj->js (merge {:inverted true} props))))

(defn qr-code-viewer [{:keys [style hint-style footer-style footer-button
                              value hint legend background-color]}]
  (println :FOO background-color)
  (if value
    (let [{:keys [width]} @(re-frame/subscribe [:dimensions/window])]
      [react/view {:style (merge styles/qr-code style)}
       (when width
         (let [size (int (min width styles/qr-code-max-width))]
           [react/view {:style               (styles/qr-code-container size background-color)
                        :accessibility-label :qr-code-image}
            [qr-code {:value value
                      :size  size}]]))
       [react/text {:style (merge styles/qr-code-hint hint-style)}
        hint]
       [react/view styles/footer
        [react/view styles/wallet-info
         [react/text {:style               (merge styles/hash-value-text footer-style)
                      :accessibility-label :address-text
                      :selectable          true}
          legend]]]
       (when footer-button
         [footer-button value])])
    [react/view [react/text "no value"]]))
