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
            [status-im.utils.logging :as log]))

(defn qr-scanner-toolbar []
  [toolbar {:title            (label :t/new-contact)
            :background-color toolbar-background2}])

(defview qr-scanner []
  []
  [view st/barcode-scanner-container
   [qr-scanner-toolbar]
   [camera {:onBarCodeRead (fn [{:keys [name address whisper-identity phone-number] :as contact}]
                             (when name (dispatch [:set-in [:new-contact :name] name]))
                             (when address (dispatch [:set-in [:new-contact :address] address]))
                             (when whisper-identity (dispatch [:set-in [:new-contact :whisper-identity] whisper-identity]))
                             (when phone-number (dispatch [:set-in [:new-contact :phone-number] phone-number]))
                             (dispatch [:navigate-back]))
            :style st/barcode-scanner}]
   [view st/rectangle-container
    [view st/rectangle
     [image {:source {:uri :corner_left_top}
             :style  st/corner-left-top}]
     [image {:source {:uri :corner_right_top}
             :style  st/corner-right-top}]
     [image {:source {:uri :corner_right_bottom}
             :style  st/corner-right-bottom}]
     [image {:source {:uri :corner_left_bottom}
             :style  st/corner-left-bottom}]]]
   ])