(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                image]]
            [status-im.components.camera :refer [camera]]
            [status-im.components.styles :refer [icon-search
                                                 icon-back]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.actions :as act]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.ui.screens.qr-scanner.styles :as st]
            [status-im.utils.types :refer [json->clj]]
            [clojure.string :as str]))

(defview qr-scanner-toolbar [title]
  (letsubs [modal [:get :modal]]
    [view
     [status-bar]
     [toolbar {:title            title
               :background-color toolbar-background1
               :nav-action       (when modal
                                   (act/back #(dispatch [:navigate-back])))}]]))

(defview qr-scanner []
  (letsubs [identifier [:get :current-qr-context]]
    [view st/barcode-scanner-container
     [qr-scanner-toolbar (:toolbar-title identifier)]
     [camera {:onBarCodeRead (fn [code]
                               (let [data (-> (.-data code)
                                              (str/replace #"ethereum:" ""))]
                                 (dispatch [:set-qr-code identifier data])))
              ;:barCodeTypes  [:qr]
              :captureAudio  false
              :style         st/barcode-scanner}]
     [view st/rectangle-container
      [view st/rectangle
       [image {:source {:uri :corner_left_top}
               :style  st/corner-left-top}]
       [image {:source {:uri :corner_right_top}
               :style  st/corner-right-top}]
       [image {:source {:uri :corner_right_bottom}
               :style  st/corner-right-bottom}]
       [image {:source {:uri :corner_left_bottom}
               :style  st/corner-left-bottom}]]]]))
