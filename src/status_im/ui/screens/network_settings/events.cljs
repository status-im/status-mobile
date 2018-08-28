(ns status-im.ui.screens.network-settings.events
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.models.network :as models.network]
            status-im.ui.screens.network-settings.edit-network.events
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.ethereum.core :as utils]))

;; handlers

(handlers/register-handler-fx
 :close-application
 (fn [_ _]
   {:close-application nil}))

(handlers/register-handler-fx
 ::save-network
 (fn [{:keys [db now] :as cofx} [_ network]]
   (handlers-macro/merge-fx cofx
                            (accounts.utils/account-update {:network      network
                                                            :last-updated now}
                                                           [::close-application]))))

(handlers/register-handler-fx
 ::remove-network
 (fn [{:keys [db now] :as cofx} [_ network]]
   (let [networks         (dissoc (get-in db [:account/account :networks]) network)]
     (handlers-macro/merge-fx cofx
                              {:dispatch [:navigate-back]}
                              (accounts.utils/account-update {:networks     networks
                                                              :last-updated now})))))

(handlers/register-handler-fx
 :connect-network
 (fn [cofx [_ network]]
   (models.network/connect cofx {:network network})))

(handlers/register-handler-fx
 :delete-network
 (fn [cofx [_ network]]
   (models.network/delete cofx {:network network})))
