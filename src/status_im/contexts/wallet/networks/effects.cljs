(ns status-im.contexts.wallet.networks.effects
  (:require [promesa.core :as promesa]
            [status-im.common.json-rpc.events :as rpc]
            [status-im.contexts.wallet.networks.new-networks :as new-networks]
            [utils.re-frame :as rf]))

(rf/reg-fx
 :effects.wallet/get-networks
 (fn [{:keys [on-success on-error]}]
   (-> (rpc/call-async "wallet_getFlatEthereumChains" false)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx
 :effects.wallet/set-network-active
 (fn [{:keys [chain-id active? on-success on-error]}]
   (-> (rpc/call-async "wallet_setChainActive"
                       true
                       chain-id
                       active?)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx :effects.wallet/deactivate-and-activate-another-network
 (fn [{:keys [deactivate-chain-id activate-chain-id on-success on-error]}]
   (-> (promesa/do
         (rpc/call-async "wallet_setChainActive" true deactivate-chain-id false)
         (rpc/call-async "wallet_setChainActive" true activate-chain-id true))
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet/new-networks-seen?
 (fn [{:keys [chain-ids on-success on-error]}]
   (-> (new-networks/marked-as-seen? chain-ids)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet/store-new-networks-as-seen
 (fn [{:keys [chain-ids on-success on-error]}]
   (-> (new-networks/mark-as-seen chain-ids)
       (promesa/then on-success)
       (promesa/then on-error))))
