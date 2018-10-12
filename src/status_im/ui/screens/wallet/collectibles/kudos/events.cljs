(ns status-im.ui.screens.wallet.collectibles.kudos.events
  (:require [status-im.ui.screens.wallet.collectibles.events :as collectibles]
            [status-im.utils.http :as http]
            [status-im.utils.ethereum.erc721 :as erc721]
            [status-im.utils.ethereum.tokens :as tokens]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.constants :as constants]
            [status-im.utils.ethereum.core :as ethereum]))

(def kudos :KUDOS)

(defmethod collectibles/load-collectible-fx kudos [{db :db} symbol id]
  (let [chain-id (get-in constants/default-networks [(:network db) :config :NetworkId])]
    {:erc721-token-uri [(:web3 db) symbol id chain-id]}))

(re-frame/reg-fx
 :erc721-token-uri
 (fn [[web3 symbol tokenId chain-id]]
   (let [chain (ethereum/chain-id->chain-keyword chain-id)
         contract (:address (tokens/symbol->token chain symbol))]
     (erc721/token-uri web3 contract tokenId
                       #(re-frame/dispatch [:token-uri-success
                                            tokenId
                                            (when %2
                                              ;;TODO extra chars in rinkeby
                                              (subs %2 (.indexOf %2 "http")))])))))

(handlers/register-handler-fx
 :token-uri-success
 (fn [_ [_ tokenId token-uri]]
   {:http-get {:url                    token-uri
               :success-event-creator (fn [o]
                                        [:load-collectible-success kudos {tokenId (http/parse-payload o)}])
               :failure-event-creator (fn [o]
                                        [:load-collectible-failure kudos {tokenId (http/parse-payload o)}])}}))
