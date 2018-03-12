(ns status-im.ui.components.common.common
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.common.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.colors :as colors]))

(defn top-shadow []
  (when platform/android?
    [react/view]))

(defn bottom-shadow []
  (when platform/android?
    [react/view]))

(defn separator [style & [wrapper-style]]
  [react/view (merge styles/separator-wrapper wrapper-style)
   [react/view (merge styles/separator style)]])

(defn list-separator []
  [separator styles/list-separator])

(defn list-footer []
  [react/view styles/list-header-footer-spacing])

(defn list-header []
  [react/view styles/list-header-footer-spacing])

(defn form-title [label & [{:keys [count-value]}]]
  [react/view
   [react/view styles/form-title-container
    [react/view styles/form-title-inner-container
     [react/text {:style styles/form-title
                  :font  :medium}
      label]
     (when-not (nil? count-value)
       [react/text {:style styles/form-title-count
                    :font  :medium}
        count-value])]]
   [top-shadow]])

(defview network-info [{:keys [text-color]}]
  (letsubs [network-id [:get-network-id]]
    [react/view
     [react/view styles/network-container
      [react/view styles/network-icon
       [vector-icons/icon :icons/network {:color :white}]]
      [react/text {:style (styles/network-text text-color)}
       (if (ethereum/testnet? network-id)
         (i18n/label :t/testnet-text {:testnet (get-in ethereum/chains [(ethereum/chain-id->chain-keyword network-id) :name] "Unknown")})
         (i18n/label :t/mainnet-text))]]]))

(defn logo
  ([] (logo nil))
  ([{:keys [size icon-size shadow?] :or {shadow? true}}]
   [react/view {:style (styles/logo-container size shadow?)}
    [icons/icon :icons/logo (styles/logo icon-size)]]))

(defn bottom-button [{:keys [label disabled? on-press forward?]}]
  [react/touchable-highlight {:on-press on-press :disabled disabled?}
   [react/view (styles/bottom-button disabled?)
    [react/text {:style      styles/bottom-button-label
                 :uppercase? true}
     (or label (i18n/label :t/next))]
    (when forward?
      [icons/icon :icons/forward {:color colors/blue}])]])

(defn button [{:keys [on-press label background? button-style label-style] :or {background? true}}]
  [react/touchable-highlight {:on-press on-press}
   [react/view {:style (styles/button button-style background?)}
    [react/text {:uppercase? true
                 :style      (merge styles/button-label label-style)}
     label]]])

(defn counter
  ([value] (counter nil value))
  ([{:keys [size accessibility-label] :or {size 18}} value]
   [react/view {:style (styles/counter-container size)}
    [react/text (cond-> {:style (styles/counter-label size)}
                  accessibility-label
                  (assoc :accessibility-label accessibility-label))
     value]]))

(defn image-contain [_ _]
  (let [content-width (reagent/atom 0)]
    (reagent/create-class
      {:reagent-render
       (fn [{:keys [container-style style]} {:keys [image width height]}]
         [react/view {:style     (merge styles/image-contain container-style)
                      :on-layout #(reset! content-width (-> % .-nativeEvent .-layout .-width))}
          [react/image {:source      image
                        :resize-mode :contain
                        :style       (merge style
                                            {:width  @content-width
                                             :height (/ (* @content-width height) width)})}]])})))