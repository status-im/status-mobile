(ns status-im.ui.screens.wallet.collectibles.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.ethereum.erc721 :as erc721]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.money :as money]
            [status-im.browser.core :as browser]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.constants :as constants]))

(defmulti load-collectible-fx (fn [_ symbol _] symbol))

(defmethod load-collectible-fx :default [_ _ _] nil)

(defmulti load-collectibles-fx (fn [_ symbol _ _] symbol))

(defmethod load-collectibles-fx :default [web3 symbol items-number address chain-id]
  {:load-collectibles-fx [web3 symbol items-number address chain-id]})

(handlers/register-handler-fx
 :show-collectibles-list
 (fn [{:keys [db]} [_ address {:keys [symbol amount] :as collectible}]]
   (let [chain-id (get-in constants/default-networks [(:network db) :config :NetworkId])
         items-number        (money/to-number amount)
         loaded-items-number (count (get-in db [:collectibles symbol]))]
     (merge (when (not= items-number loaded-items-number)
              (load-collectibles-fx (:web3 db) symbol items-number address chain-id))
            {:dispatch [:navigate-to :collectibles-list collectible]}))))

(defn load-token [web3 i items-number contract address symbol]
  (when (< i items-number)
    (erc721/token-of-owner-by-index web3 contract address i
                                    (fn [v1 v2]
                                      (load-token web3 (inc i) items-number contract address symbol)
                                      (re-frame/dispatch [:load-collectible symbol (.toNumber v2)])))))

(re-frame/reg-fx
 :load-collectibles-fx
 (fn [[web3 symbol items-number address chain-id]]
   (let [chain (ethereum/chain-id->chain-keyword chain-id)
         contract (:address (tokens/symbol->token chain symbol))]
     (load-token web3 0 items-number contract address symbol))))

(handlers/register-handler-fx
 :load-collectible
 (fn [cofx [_ symbol token-id]]
   (load-collectible-fx cofx symbol token-id)))

(handlers/register-handler-fx
 :store-collectibles
 (fn [{db :db} [_ symbol collectibles]]
   {:db (update-in db [:collectibles symbol] merge
                   (reduce #(assoc %1 (:tokenId %2) %2) {} collectibles))}))

(handlers/register-handler-fx
 :load-collectible-success
 (fn [{db :db} [_ symbol collectibles]]
   {:db (update-in db [:collectibles symbol] merge collectibles)}))

(handlers/register-handler-fx
 :load-collectibles-failure
 (fn [{db :db} [_ reason]]
   {:db (update-in db [:collectibles symbol :errors] merge reason)}))

(handlers/register-handler-fx
 :load-collectible-failure
 (fn [{db :db} [_]]
   {:db db}))

(handlers/register-handler-fx
 :open-collectible-in-browser
 (fn [cofx [_ url]]
   (browser/open-url cofx url)))
