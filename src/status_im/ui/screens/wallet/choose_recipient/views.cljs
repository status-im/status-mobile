(ns status-im.ui.screens.wallet.choose-recipient.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.utils.utils :as utils]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.styles :as wallet.styles]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.wallet.choose-recipient.styles :as styles]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.camera :as camera]
            [clojure.string :as string]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn choose-from-contacts []
  (re-frame/dispatch [:navigate-to-modal
                      :contact-list-modal
                      {:handler #(re-frame/dispatch [:wallet-open-send-transaction (:address %1) (:name %1)])
                       :action  :send
                       :params  {:hide-actions? true}}]))

(defn toolbar-view []
  [toolbar/toolbar2 {:style        wallet.styles/toolbar
                     :no-sync-bar? true}
   [toolbar/nav-button (act/back-white act/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/wallet-choose-recipient)]
   [toolbar/actions [{:icon      :icons/flash-active
                      :icon-opts {:color :white}
                      :handler   show-not-implemented!}]]])

(defn recipient-buttons []
  [react/view {:style styles/recipient-buttons}
   [react/touchable-highlight {:style (styles/recipient-touchable true)
                               :on-press choose-from-contacts}
    [react/view {:style styles/recipient-button}
     [react/text {:style styles/recipient-button-text}
      (i18n/label :t/wallet-choose-from-contacts)]
     [vector-icons/icon :icons/qr {:color           :white
                                   :container-style styles/recipient-icon}]]]
   [react/touchable-highlight {:style    (styles/recipient-touchable true)
                               :on-press #(react/get-from-clipboard
                                            (fn [clipboard]
                                              (re-frame/dispatch [:choose-recipient clipboard])))}
    [react/view {:style styles/recipient-button}
     [react/text {:style styles/recipient-button-text}
      (i18n/label :t/wallet-address-from-clipboard)]
     [vector-icons/icon :icons/copy-from {:color           :white
                                          :container-style styles/recipient-icon}]]]
   [react/touchable-highlight {:style styles/recipient-touchable-disabled}
    [react/view {:style styles/recipient-button}
     [react/text {:style styles/recipient-button-text-disabled}
      (i18n/label :t/wallet-browse-photos)]
     [vector-icons/icon :icons/browse {:color           :white
                                       :container-style styles/recipient-icon-disabled}]]]])

(defn viewfinder [{:keys [height width]}]
  (let [min-dimension (min height width)]
    [react/view {:style styles/viewfinder-port}
     [react/view {:style (styles/viewfinder-translucent height width :top)}]
     [react/view {:style (styles/viewfinder-translucent height width :right)}]
     [react/view {:style (styles/viewfinder-translucent height width :bottom)}]
     [react/view {:style (styles/viewfinder-translucent height width :left)}]
     [react/image {:source {:uri :corner_left_top}
                   :style  (styles/corner-left-top min-dimension)}]
     [react/image {:source {:uri :corner_right_top}
                   :style  (styles/corner-right-top min-dimension)}]
     [react/image {:source {:uri :corner_left_bottom}
                   :style  (styles/corner-left-bottom min-dimension)}]
     [react/image {:source {:uri :corner_right_bottom}
                   :style  (styles/corner-right-bottom min-dimension)}]]))

(defview choose-recipient []
  (letsubs [camera-dimensions [:camera-dimensions]]
    [react/view {:style styles/wallet-container}
     [status-bar/status-bar {:type :wallet}]
     [toolbar-view]
     [react/view {:style     styles/qr-container
                  :on-layout #(let [layout (.. % -nativeEvent -layout)]
                                (re-frame/dispatch [:set-in [:wallet :camera-dimensions]
                                                    {:width  (.-width layout)
                                                     :height (.-height layout)}]))}
      [camera/camera {:style         styles/preview
                      :aspect        :fill
                      :captureAudio  false
                      :onBarCodeRead (fn [code]
                                       (let [data (-> code
                                                      .-data
                                                      (string/replace #"ethereum:" ""))]
                                         (re-frame/dispatch [:choose-recipient data])))}]
      [viewfinder camera-dimensions]]
     [recipient-buttons]]))