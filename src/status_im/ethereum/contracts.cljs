(ns status-im.ethereum.contracts
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.abi-spec :as abi-spec]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]))

(def contracts
  {:status/tribute-to-talk
   {:address
    {:mainnet nil
     :testnet "0x3da3fc53e24707f36c5b4433b442e896c4955f0e"
     :rinkeby nil}
    :methods
    {:get-manifest
     {:signature "getManifest(address)"
      :outputs ["bytes"]}
     :set-manifest
     {:signature "setManifest(bytes)"
      :write? true}}}})

(re-frame/reg-fx
 ::call
 (fn [{:keys [address data callback]}]
   (ethereum/call {:to address
                   :data data}
                  callback)))

(defn get-contract-address
  [db contract]
  (let [chain-keyword (-> (get-in db [:account/account :networks (:network db)])
                          ethereum/network->chain-keyword)]
    (get-in contracts [contract :address chain-keyword])))

(fx/defn call
  [{:keys [db] :as cofx}
   {:keys [contract contract-address method params
           callback on-result on-error details]}]
  (when-let [contract-address (or contract-address
                                  (get-contract-address db contract))]
    (let [{:keys [signature outputs write?]}
          (get-in contracts [contract :methods method])
          data (abi-spec/encode signature params)]
      (if write?
        (wallet/open-sign-transaction-flow
         cofx
         (merge {:to        contract-address
                 :data      data
                 :id        "approve"
                 :symbol    :ETH
                 :method    "eth_sendTransaction"
                 :amount    (money/bignumber 0)
                 :on-result on-result
                 :on-error  on-error}
                details))
        {::call {:address  contract-address
                 :data     data
                 :callback #(callback (if (empty? outputs)
                                        %
                                        (abi-spec/decode % outputs)))}}))))
