(ns status-im.ui.components.common.common
  (:require [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.platform :as platform])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

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
     [react/text {:style styles/form-title}
      label]
     (when-not (nil? count-value)
       [react/text {:style styles/form-title-count}
        count-value])]]
   [top-shadow]])

(defview network-info [{:keys [text-color]}]
  (letsubs [network-id [:get-network-id]]
    [react/view
     [react/view styles/network-container
      [react/view styles/network-icon
       [vector-icons/icon :main-icons/network {:color :white}]]
      [react/text {:style (styles/network-text text-color)}
       (cond (ethereum/testnet? network-id)
             (i18n/label :t/testnet-text {:testnet (get-in ethereum/chains [(ethereum/chain-id->chain-keyword network-id) :name] "Unknown")})

             (ethereum/sidechain? network-id)
             (i18n/label :t/sidechain-text {:sidechain (get-in ethereum/chains [(ethereum/chain-id->chain-keyword network-id) :name] "Unknown")})

             :else
             (i18n/label :t/mainnet-text))]]]))

(defn logo
  [{:keys [size icon-size]}]
  [react/view {:style (styles/logo-container size)}
   [vector-icons/icon :icons/logo (styles/logo icon-size)]])

(defn bottom-button [{:keys [accessibility-label
                             label
                             disabled?
                             on-press
                             forward?
                             back?]}]
  [react/touchable-highlight {:on-press on-press :disabled disabled?}
   [react/view (styles/bottom-button disabled?)
    (when back?
      [vector-icons/icon :main-icons/back {:color colors/blue
                                           :container-style {:align-self :baseline}}])
    [react/text {:style      styles/bottom-button-label
                 :accessibility-label accessibility-label}
     (or label (i18n/label :t/next))]
    (when forward?
      [vector-icons/icon :main-icons/next {:color colors/blue}])]])

(defn button [{:keys [on-press label background? button-style label-style disabled?] :or {background? true disabled false}}]
  [react/touchable-highlight {:style (styles/button button-style background? disabled?)
                              :on-press on-press
                              :disabled disabled?}
   [react/text {:style (merge styles/button-label label-style)}
    label]])

(defn red-button [props]
  [react/view {:align-items :center}
   [button (merge props
                  {:label-style {:color colors/red :font-size 15}
                   :button-style {:padding-horizontal 32 :background-color  colors/red-light}})]])

(defn counter
  ([value] (counter nil value))
  ([{:keys [size accessibility-label] :or {size 18}} value]
   (let [more-than-9 (> value 9)]
     [react/view {:style (styles/counter-container size)}
      [react/text (cond-> {:style (styles/counter-label size)}
                    accessibility-label
                    (assoc :accessibility-label accessibility-label))
       (if more-than-9 (i18n/label :t/counter-9-plus) value)]])))

(defview image-contain [{:keys [container-style style]} {:keys [image width height]}]
  (letsubs [content-width (reagent/atom 0)
            {window-width :width window-height :height} [:dimensions/window]]
    [react/view {:style     (merge styles/image-contain container-style)
                 :on-layout #(reset! content-width (-> % .-nativeEvent .-layout .-width))}
     [react/image {:source      image
                   :resize-mode :contain
                   :style       (merge style
                                       (if (> window-height window-width)
                                         {:width  (* @content-width
                                                     (if (< window-height 600)
                                                       0.6
                                                       1))
                                          :height (/ (* @content-width height
                                                        (if (< window-height 600)
                                                          0.6
                                                          1))
                                                     width)}
                                         {:width  @content-width
                                          :height (* window-height 0.6)}))}]]))
