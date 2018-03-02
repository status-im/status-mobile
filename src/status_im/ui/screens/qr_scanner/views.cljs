(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.qr-scanner.styles :as styles]
            [status-im.ui.components.common.styles :as common.styles]))

(defview qr-scanner-toolbar [title hide-nav?]
  (letsubs [modal [:get :modal]]
    [react/view
     [toolbar/simple-toolbar title]]))

(defview ^:theme qr-scanner []
  (letsubs [{identifier :current-qr-context} [:get-screen-params]
            camera-initialized? (reagent/atom false)]
    [react/view common.styles/flex
     [qr-scanner-toolbar (:toolbar-title identifier) (not @camera-initialized?)]
     [camera/camera {:onBarCodeRead #(re-frame/dispatch [:set-qr-code identifier (camera/get-qr-code-data %)])
                     :ref           #(reset! camera-initialized? true)
                     :captureAudio  false
                     :style         styles/barcode-scanner}]
     [react/view styles/rectangle-container
      [react/view styles/rectangle
       [react/image {:source {:uri :corner_left_top}
                     :style  styles/corner-left-top}]
       [react/image {:source {:uri :corner_right_top}
                     :style  styles/corner-right-top}]
       [react/image {:source {:uri :corner_right_bottom}
                     :style  styles/corner-right-bottom}]
       [react/image {:source {:uri :corner_left_bottom}
                     :style  styles/corner-left-bottom}]]]]))
