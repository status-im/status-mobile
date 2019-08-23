(ns status-im.ui.screens.wallet.choose-recipient.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.wallet.choose-recipient.styles :as styles]
            [status-im.utils.platform :as platform]))

(defn- toolbar-view [camera-flashlight]
  [toolbar/toolbar
   {:transparent? true}
   [toolbar/nav-button (actions/back-white actions/default-handler)]
   [toolbar/content-title {:color :white}
    (i18n/label :t/wallet-choose-recipient)]
   [toolbar/actions [{:icon      (if (= :on camera-flashlight)
                                   :main-icons/flash-active
                                   :main-icons/flash-inactive)
                      :icon-opts {:color :white}
                      :handler   #(re-frame/dispatch [:wallet/toggle-flashlight])}]]])

(defn- viewfinder [{:keys [height width]} size]
  (let [height (cond-> height
                 platform/iphone-x? (- 78))]
    [react/view {:style styles/viewfinder-port}
     [react/view {:style (styles/viewfinder-translucent height width size :top)}]
     [react/view {:style (styles/viewfinder-translucent height width size :right)}]
     [react/view {:style (styles/viewfinder-translucent height width size :bottom)}]
     [react/view {:style (styles/viewfinder-translucent height width size :left)}]
     [react/image {:source {:uri :corner_left_top}
                   :style  (styles/corner-left-top height width size)}]
     [react/image {:source {:uri :corner_right_top}
                   :style  (styles/corner-right-top height width size)}]
     [react/image {:source {:uri :corner_left_bottom}
                   :style  (styles/corner-left-bottom height width size)}]
     [react/image {:source {:uri :corner_right_bottom}
                   :style  (styles/corner-right-bottom height width size)}]]))

(defn- size [{:keys [height width]}]
  (int (* 2 (/ (min height width) 3))))

(defview choose-recipient []
  (letsubs [read-once?        (atom false)
            dimensions        [:dimensions/window]
            camera-flashlight [:wallet.send/camera-flashlight]]
    [react/view {:style styles/qr-code}
     [status-bar/status-bar {:type :transparent}]
     [toolbar-view camera-flashlight]
     [react/text {:style               (styles/qr-code-text dimensions)
                  :accessibility-label :scan-qr-code-with-wallet-address-text}
      (i18n/label :t/scan-qr-code)]
     [react/view {:style          styles/qr-container
                  :pointer-events :none}
      [react/with-activity-indicator
       {}
       [camera/camera {:style         styles/preview
                       :aspect        :fill
                       :captureAudio  false
                       ;:torchMode     (camera/set-torch camera-flashlight)
                       :onBarCodeRead #(when-not @read-once?
                                         (reset! read-once? true)
                                         (re-frame/dispatch [:wallet/fill-request-from-url (camera/get-qr-code-data %) :qr]))}]]
      [viewfinder dimensions (size dimensions)]]
     [bottom-buttons/bottom-button
      [button/button {:disabled?           false
                      :on-press            #(re-frame/dispatch [:navigate-back])
                      :accessibility-label :cancel-button}
       (i18n/label :t/cancel)]]]))
