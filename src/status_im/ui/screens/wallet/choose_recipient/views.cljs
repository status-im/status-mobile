(ns status-im.ui.screens.wallet.choose-recipient.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as act]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.choose-recipient.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.styles :as wallet.styles]))

(defn choose-from-contacts []
  (re-frame/dispatch [:navigate-to-modal
                      :contact-list-modal
                      {:handler #(re-frame/dispatch [:wallet/fill-request-from-contact %])
                       :action  :send
                       :params  {:hide-actions? true}}]))

(defn toolbar-view [camera-flashlight]
  [toolbar/toolbar {:style wallet.styles/toolbar}
   [toolbar/nav-button (act/back-white act/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/wallet-choose-recipient)]
   [toolbar/actions [{:icon      (if (= :on camera-flashlight) :icons/flash-active
                                     :icons/flash-inactive)
                      :icon-opts {:color :white}
                      :handler #(re-frame/dispatch [:wallet/toggle-flashlight])}]]])

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
                                              (re-frame/dispatch [:wallet/fill-request-from-url (string/trim-newline clipboard) nil])))}
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
  (letsubs [camera-dimensions [:wallet.send/camera-dimensions]
            camera-flashlight [:wallet.send/camera-flashlight]
            camera-permitted? [:wallet.send/camera-permitted?]]
    [react/view {:style styles/wallet-container}
     [status-bar/status-bar {:type :wallet}]
     [toolbar-view camera-flashlight]
     [react/view {:style         styles/qr-container
                  :pointerEvents :none
                  :on-layout     #(let [layout (.. % -nativeEvent -layout)]
                                    (re-frame/dispatch [:wallet.send/set-camera-dimensions
                                                        {:width  (.-width layout)
                                                         :height (.-height layout)}]))}
      (when (or platform/android?
                camera-permitted?)
        [camera/camera {:style         styles/preview
                        :aspect        :fill
                        :captureAudio  false
                        :torchMode     (camera/set-torch camera-flashlight)
                        :onBarCodeRead #(re-frame/dispatch [:wallet/fill-request-from-url (camera/get-qr-code-data %) nil])}])
      [viewfinder camera-dimensions]]
     [recipient-buttons]]))
