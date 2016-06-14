(ns status-im.qr-scanner.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                image]]
            [status-im.components.camera :refer [camera]]
            [status-im.components.styles :refer [toolbar-background1
                                                 icon-search]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.qr-scanner.styles :as st]
            [status-im.utils.types :refer [json->clj]]))

(defn qr-scanner-toolbar [title]
  [toolbar {:title            title
            :background-color toolbar-background1
            :action           {:image   {:source {:uri :icon_lock_white}
                                         :style  icon-search}
                               :handler #()}}])

(defview qr-scanner []
  [identifier [:get :current-qr-context]]
  [view st/barcode-scanner-container
   [qr-scanner-toolbar (:toolbar-title identifier)]
   [camera {;:on-bar-code-read #(js/alert "ok")
            :onBarCodeRead #(let [data (json->clj (.-data %))]
                             (dispatch [:set-qr-code identifier data]))
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
             :style  st/corner-left-bottom}]]]])
