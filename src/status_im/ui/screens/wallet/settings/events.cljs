(ns status-im.ui.screens.wallet.settings.events
  (:require [status-im.ui.screens.wallet.settings.models :as models]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :wallet.settings/toggle-visible-token
 (fn [cofx [_ symbol checked?]]
   (models/toggle-visible-token symbol checked? accounts.update/update-settings cofx)))

(handlers/register-handler-fx
 :configure-token-balance-and-visibility
 (fn [cofx [_ symbol balance]]
   (models/configure-token-balance-and-visibility symbol balance accounts.update/update-settings cofx)))
