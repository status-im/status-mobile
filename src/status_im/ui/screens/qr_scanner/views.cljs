(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.qr-scanner.styles :as styles]))

(defview qr-scanner-toolbar [title hide-nav?]
  (letsubs [modal [:get :modal]]
    [react/view
     [status-bar/status-bar]
     [toolbar/simple-toolbar title]]))

(defn on-barcode-read [identifier data]
  (re-frame/dispatch [:qr-scanner.callback/scan-qr-code-success identifier (camera/get-qr-code-data data)]))

(defview qr-scanner []
  (letsubs [{identifier :current-qr-context} [:get-screen-params]
            camera-initialized? (reagent/atom false)
            barcode-read? (reagent/atom false)]
    [react/view styles/barcode-scanner-container
     [qr-scanner-toolbar (or (:toolbar-title identifier) (i18n/label :t/scan-qr)) (not @camera-initialized?)]
     [camera/camera {:onBarCodeRead #(if (:multiple? identifier)
                                       (on-barcode-read identifier %)
                                       (when-not @barcode-read?
                                         (do (reset! barcode-read? true)
                                             (on-barcode-read identifier %))))
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
