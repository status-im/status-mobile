(ns status-im.qr-scanner.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                icon
                                                text
                                                image
                                                touchable-highlight]]
            [status-im.components.camera :refer [camera]]
            [status-im.components.styles :refer [toolbar-background2]]
            [status-im.components.toolbar :refer [toolbar]]
            [status-im.components.icons.ionicons :refer [icon]]
            [status-im.components.styles :refer [color-blue]]
            [status-im.i18n :refer [label]]
            [status-im.qr-scanner.styles :as st]
            [status-im.utils.types :refer [json->clj]]
            [status-im.utils.logging :as log]))

(defn qr-scanner-toolbar []
  [toolbar {:title            (label :t/new-contact)
            :background-color toolbar-background2}])

(defview qr-scanner []
  [identifier [:get :current-qr-context]]
  [view st/barcode-scanner-container
   [qr-scanner-toolbar]
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
