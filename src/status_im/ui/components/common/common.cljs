(ns status-im.ui.components.common.common
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.context-menu :refer [context-menu]]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.common.styles :as styles]
            [status-im.i18n :as i18n]))

(defn top-shadow []
  (when platform/android?
    [react/linear-gradient
     {:style  styles/gradient-bottom
      :colors styles/gradient-top-colors}]))

(defn bottom-shadow []
  (when platform/android?
    [react/linear-gradient
     {:style  styles/gradient-top
      :colors styles/gradient-bottom-colors}]))

(defn separator [style & [wrapper-style]]
  [react/view (merge styles/separator-wrapper wrapper-style)
   [react/view (merge styles/separator style)]])

(defn form-spacer []
  [react/view
   [bottom-shadow]
   [react/view styles/form-spacer]
   [top-shadow]])

(defn list-separator []
  [separator styles/list-separator])

(defn list-footer []
  [react/view styles/list-header-footer-spacing])

(defn list-header []
 [react/view styles/list-header-footer-spacing])

(defn form-title [label & [{:keys [count-value extended? options]}]]
  [react/view
   [react/view styles/form-title-container
    [react/view styles/form-title-inner-container
     [react/text {:style styles/form-title
                  :font  :medium}
      label]
     (when-not (nil? count-value)
       [react/text {:style styles/form-title-count
                    :font  :medium}
        count-value])]
    (when extended?
      [react/view
       [react/view {:flex 1}]])
    (when extended?
      [react/view styles/form-title-extend-container
       [context-menu
        [vector-icons/icon :icons/options]
        options
        nil
        styles/form-title-extend-button]])]
   [top-shadow]])

(defview network-info [{:keys [text-color]}]
  (letsubs [testnet?     [:testnet?]
            testnet-name [:testnet-name]]
    [react/view
     [react/view styles/network-container
      [react/view styles/network-icon
       [vector-icons/icon :icons/network {:color :white}]]
      [react/text {:style (styles/network-text text-color)}
       (if testnet?
         (i18n/label :t/testnet-text {:testnet testnet-name})
         (i18n/label :t/mainnet-text))]]]))
