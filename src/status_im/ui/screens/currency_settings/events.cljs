(ns status-im.ui.screens.currency-settings.events
  (:require [status-im.ui.screens.currency-settings.models :as models]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :wallet.settings/set-currency
 (fn [cofx [_ currency]]
   (models/set-currency currency cofx)))
