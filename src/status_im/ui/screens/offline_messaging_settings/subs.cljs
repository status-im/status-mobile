(ns status-im.ui.screens.offline-messaging-settings.subs
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.offline-messaging-settings.edit-mailserver.subs
            [status-im.utils.ethereum.core :as ethereum]))

(re-frame/reg-sub :settings/current-wnode
                  :<- [:network]
                  :<- [:get-current-account]
                  (fn [[network current-account]]
                    (let [chain (ethereum/network->chain-keyword network)]
                      (get-in current-account [:settings :wnode chain]))))

(re-frame/reg-sub :settings/network-wnodes
                  :<- [:network]
                  :<- [:get :inbox/wnodes]
                  (fn [[network wnodes]]
                    (let [chain (ethereum/network->chain-keyword network)]
                      (chain wnodes))))
