(ns status-im.ui.screens.wallet.settings.events
  (:require [status-im.ui.screens.wallet.settings.models :as models]

            [status-im.utils.handlers :as handlers]))

(handlers/register-handler-fx
 :wallet.settings/toggle-visible-token
 (fn [cofx [_ symbol checked?]]
   (models/toggle-visible-token cofx symbol checked?)))

(handlers/register-handler-fx
 :configure-token-balance-and-visibility
 (fn [cofx [_ symbol balance]]
   (models/configure-token-balance-and-visibility cofx symbol balance)))

(handlers/register-handler-fx
 :wallet.ui/pull-to-refresh
 (fn [cofx _]
   (models/wallet-autoconfig-tokens cofx)))
