(ns status-im.utils.ethereum.contracts
  (:require [re-frame.core :as re-frame]
            [status-im.models.wallet :as wallet]
            [status-im.utils.ethereum.abi-spec :as abi-spec]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]))

(def contracts
  {:status/tribute-to-talk
   {:address
    {:mainnet nil
     :testnet "0x3da3fc53e24707f36c5b4433b442e896c4955f0e"
     :rinkeby nil}
    :methods
    {:get-manifest
     {:signature "getManifest(address)"
      :return-params ["bytes"]}
     :set-manifest
     {:signature "setManifest(bytes)"
      :write? true}}}
   :status/sticker-market
   {:address
    {:mainnet nil
     :testnet "0x39d16CdB56b5a6a89e1A397A13Fe48034694316E"
     :rinkeby nil}
    :methods
    {:pack-count
     {:signature "packCount()"
      :return-params ["uint256"]}
     :pack-data
     {:signature "getPackData(uint256)"
      :return-params ["bytes4[]" "address" "bool" "uint256" "uint256" "bytes"]}}}})

(re-frame/reg-fx
 ::call
 (fn [{:keys [address data callback]}]
   (ethereum/call {:to address
                   :data data}
                  callback)))

(fx/defn call
  [{:keys [db] :as cofx} {:keys [contract method params callback on-result]}]
  (let [chain-keyword (-> (get-in db [:account/account :networks (:network db)])
                          ethereum/network->chain-keyword)
        contract-address (get-in contracts [contract :address chain-keyword])]
    (when contract-address
      (let [{:keys [signature return-params write?]}
            (get-in contracts [contract :methods method])
            data (abi-spec/encode signature params)]
        (if write?
          (wallet/open-sign-transaction-flow
           cofx
           {:to        contract-address
            :data      data
            :id        "approve"
            :symbol    :ETH
            :method    "eth_sendTransaction"
            :amount    (money/bignumber 0)
            :on-result on-result})
          {::call {:address  contract-address
                   :data     data
                   :callback #(callback (if (empty? return-params)
                                          %
                                          (abi-spec/decode % return-params)))}})))))
