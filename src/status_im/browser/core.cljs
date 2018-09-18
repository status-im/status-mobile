(ns status-im.browser.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn scan-qr-code
  [data message]
  {:send-to-bridge-fx [(assoc message :result data) (get-in cofx [:db :webview-bridge])]
   :dispatch          [:navigate-back]})
