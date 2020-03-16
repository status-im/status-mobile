(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as topbar]
            [status-im.ui.screens.qr-scanner.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(defn- topbar [camera-flashlight {:keys [title] :as opts}]
  [topbar/toolbar
   {:transparent? true}
   [topbar/nav-text
    {:style   {:color colors/white-persist :margin-left 16}
     :handler #(re-frame/dispatch [:qr-scanner.callback/scan-qr-code-cancel opts])}
    (i18n/label :t/cancel)]
   [topbar/content-title {:color colors/white-persist}
    (or title (i18n/label :t/scan-qr))]
   #_[topbar/actions [{:icon      (if (= :on camera-flashlight)
                                    :main-icons/flash-active
                                    :main-icons/flash-inactive)
                       :icon-opts {:color colors/white}
                       :handler   #(re-frame/dispatch [:wallet/toggle-flashlight])}]]])

(defn corner [border1 border2 corner]
  [react/view (assoc {:border-color colors/white-persist :width 60 :height 60} border1 5 border2 5 corner 32)])

(defn- viewfinder [size]
  [react/view {:style styles/viewfinder-port}
   [react/view {:width size :height size :justify-content :space-between}
    [react/view {:flex-direction :row :justify-content :space-between}
     [corner :border-top-width :border-left-width :border-top-left-radius]
     [corner :border-top-width :border-right-width :border-top-right-radius]]
    [react/view {:flex-direction :row :justify-content :space-between}
     [corner :border-bottom-width :border-left-width :border-bottom-left-radius]
     [corner :border-bottom-width :border-right-width :border-bottom-right-radius]]]])

(defn on-barcode-read [opts data]
  (re-frame/dispatch [:qr-scanner.callback/scan-qr-code-success opts (camera/get-qr-code-data data)]))

(defview qr-scanner []
  (letsubs [read-once?        (atom false)
            {:keys [height width]} [:dimensions/window]
            camera-flashlight [:wallet.send/camera-flashlight]
            opts              [:get-screen-params]]
    [react/safe-area-view {:style {:flex             1
                                   :background-color colors/black-persist}}
     [topbar camera-flashlight opts]
     [react/with-activity-indicator
      {}
      [camera/camera
       {:style         {:flex 1}
        :captureAudio  false
        :onBarCodeRead #(when-not @read-once?
                          (reset! read-once? true)
                          (on-barcode-read opts %))}]]
     [viewfinder (int (* 2 (/ (min height width) 3)))]]))
