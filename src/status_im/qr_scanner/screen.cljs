(ns status-im.qr-scanner.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                image]]
            [status-im.components.camera :refer [camera]]
            [status-im.components.styles :refer [icon-search
                                                 icon-back]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar.view :refer [toolbar]]
            [status-im.components.toolbar.styles :refer [toolbar-background1]]
            [status-im.qr-scanner.styles :as st]
            [status-im.utils.types :refer [json->clj]]
            [clojure.string :as str]))

(defview qr-scanner-toolbar [title]
  [modal [:get :modal]]
  [view
   [status-bar]
   [toolbar {:title            title
             :background-color toolbar-background1
             :nav-action       (when modal
                                 {:handler #(dispatch [:navigate-back])
                                  :image   {:source {:uri :icon_back}
                                            :style  icon-back}})
             :actions          [{:image   {:source {:uri :icon_lock_white}
                                           :style  icon-search}
                                 :handler #()}]}]])

(defview qr-scanner []
  [identifier [:get :current-qr-context]]
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
             :style  st/corner-left-bottom}]]]])
