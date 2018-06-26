(ns status-im.ui.screens.bootnodes-settings.subs
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.bootnodes-settings.edit-bootnode.subs
            [status-im.utils.ethereum.core :as ethereum]))

(re-frame/reg-sub :settings/bootnodes-enabled
                  :<- [:get :account/account]
                  (fn [account]
                    (let [{:keys [network settings]} account]
                      (get-in settings [:bootnodes network]))))

(re-frame/reg-sub :settings/network-bootnodes
                  :<- [:get :account/account]
                  (fn [account]
                    (get-in account [:bootnodes (:network account)])))
