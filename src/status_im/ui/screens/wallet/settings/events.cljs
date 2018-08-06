(ns status-im.ui.screens.wallet.settings.events
  (:require [status-im.ui.screens.wallet.settings.models :as models]
            [status-im.ui.screens.accounts.models :as accounts.models]
            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :wallet.settings/toggle-visible-token
 (fn [cofx [_ symbol checked?]]
   (models/toggle-visible-token symbol checked? accounts.models/update-settings cofx)))

(handlers/register-handler-fx
 :configure-token-balance-and-visibility
 (fn [cofx [_ symbol balance]]
   (models/configure-token-balance-and-visibility symbol balance accounts.models/update-settings cofx)))
