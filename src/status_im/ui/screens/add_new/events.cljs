(ns status-im.ui.screens.add-new.events
  (:require [status-im.ui.screens.add-new.models :as models]
            [status-im.utils.handlers :as handlers]
            [taoensso.timbre :as log]))

(handlers/register-handler-fx
 :handle-qr-code
 (fn [cofx [_ data _]]
   (models/handle-qr-code cofx data)))
