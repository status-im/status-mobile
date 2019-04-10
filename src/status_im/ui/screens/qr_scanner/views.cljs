(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.qr-scanner.styles :as styles]
            [status-im.ui.components.toolbar.actions :as actions]))

(defview qr-scanner-toolbar [title identifier]
  [react/view
   [status-bar/status-bar]
   [toolbar/toolbar {:style {:background-color :white}}
    [toolbar/nav-button (actions/back
                         #(do
                            (re-frame/dispatch [:qr-scanner.callback/scan-qr-code-cancel identifier])
                            (re-frame/dispatch [:navigate-back])))]
    [toolbar/content-title title]]])

(defn on-barcode-read [identifier data]
  (re-frame/dispatch [:qr-scanner.callback/scan-qr-code-success identifier (camera/get-qr-code-data data)]))

(defview qr-scanner []
  (letsubs [{identifier :current-qr-context
             barcode-read-sub? :barcode-read?} [:get-screen-params]
            camera-initialized? (reagent/atom false)]
    (let [barcode-read? barcode-read-sub?]
      [react/view styles/barcode-scanner-container
       [qr-scanner-toolbar (or (:toolbar-title identifier) (i18n/label :t/scan-qr)) identifier]
       [camera/camera {:onBarCodeRead #(if (:multiple? identifier)
                                         (on-barcode-read identifier %)
                                         (when-not barcode-read?
                                           (on-barcode-read identifier %)))
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
                       :style  styles/corner-left-bottom}]]]])))