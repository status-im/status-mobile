(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.components.react :as react]
            [status-im.components.camera :as camera]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.toolbar.view :as toolbar]
            [status-im.components.toolbar.actions :as action]
            [status-im.components.toolbar.styles :as toolbar.styles]
            [status-im.ui.screens.qr-scanner.styles :as styles]))

(defview qr-scanner-toolbar [title hide-nav?]
  (letsubs [modal [:get :modal]]
    [react/view
     [status-bar/status-bar]
     [toolbar/toolbar {:title            title
                       :background-color toolbar.styles/toolbar-background1
                       :hide-nav?        hide-nav?
                       :nav-action       (when modal
                                           (action/back #(re-frame/dispatch [:navigate-back])))}]]))

(defview qr-scanner []
  (letsubs [identifier [:get :current-qr-context]
            camera-initialized? (reagent/atom false)]
    [react/view styles/barcode-scanner-container
     [qr-scanner-toolbar (:toolbar-title identifier) (not @camera-initialized?)]
     [camera/camera {:onBarCodeRead #(re-frame/dispatch [:set-qr-code identifier (camera/get-qr-code-data %)])
                     ;:barCodeTypes  [:qr]
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
