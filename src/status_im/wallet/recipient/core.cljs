(ns status-im.wallet.recipient.core
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [clojure.string :as string]
            [status-im.utils.fx :as fx]
            [status-im.utils.utils :as utils]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.random :as random]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.i18n.i18n :as i18n]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.navigation :as navigation]
            [status-im.ethereum.json-rpc :as json-rpc]))

;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(re-frame/reg-fx
 ::resolve-address
 (fn [{:keys [registry ens-name cb]}]
   (ens/get-addr registry ens-name cb)))

(re-frame/reg-fx
 :wallet.recipient/address-paste
 (fn []
   (react/get-from-clipboard
    #(re-frame/dispatch [:wallet.recipient/address-changed (string/trim %)]))))

(fx/defn address-paste-pressed
  {:events [:wallet.recipient/address-paste-pressed]}
  [_]
  {:wallet.recipient/address-paste nil})

(fx/defn set-recipient
  {:events [::recipient-address-resolved]}
  [{:keys [db] :as cofx} raw-recipient id]
  (when (or (not id) (= id @resolve-last-id))
    (reset! resolve-last-id nil)
    (let [chain (ethereum/chain-keyword db)
          recipient (utils/safe-trim raw-recipient)]
      (cond
        (ethereum/address? recipient)
        (let [checksum (eip55/address->checksum recipient)]
          (if (eip55/valid-address-checksum? checksum)
            (fx/merge cofx
                      {:db       (-> db
                                     (assoc-in [:wallet/recipient :searching] false)
                                     (assoc-in [:wallet/recipient :resolved-address] checksum))}
                      (json-rpc/call
                       {:method            "eth_getCode"
                        :params            [checksum "latest"]
                        :on-success        #(when (not= "0x" %)
                                              (utils/show-popup (i18n/label :t/warning)
                                                                (i18n/label :t/warning-sending-to-contract-descr)))
                        :number-of-retries 3}))
            {:ui/show-error (i18n/label :t/wallet-invalid-address-checksum {:data recipient})
             :db (assoc-in db [:wallet/recipient :searching] false)}))
        (and (not (string/blank? recipient)) (not (string/starts-with? recipient "0x"))
             (ens/valid-eth-name-prefix? recipient))
        (let [ens-name (if (= (.indexOf ^js recipient ".") -1)
                         (stateofus/subdomain recipient)
                         recipient)]
          (if (ens/is-valid-eth-name? ens-name)
            (do
              (reset! resolve-last-id (random/id))
              {::resolve-address
               {:registry (get ens/ens-registries chain)
                :ens-name ens-name
                :cb       #(re-frame/dispatch [::recipient-address-resolved % @resolve-last-id])}})
            {:db (assoc-in db [:wallet/recipient :searching] false)}))
        :else
        {:db (assoc-in db [:wallet/recipient :searching] false)}))))

(fx/defn address-changed
  {:events [:wallet.recipient/address-changed]}
  [{:keys [db] :as cofx} new-identity]
  (fx/merge cofx
            {:db (update db :wallet/recipient assoc :address new-identity :resolved-address nil
                         :searching true)}
            (set-recipient new-identity nil)))

(fx/defn recipient-modal-closed
  {:events [:wallet/recipient-modal-closed]}
  [{:keys [db]}]
  {:db (dissoc db :wallet/recipient)})

(fx/defn add-favourite
  {:events [:wallet/add-favourite]}
  [{:keys [db] :as cofx} address name]
  (let [new-favourite {:address  address
                       :name     (or name "")}]
    (fx/merge cofx
              {:db (assoc-in db [:wallet/favourites address] new-favourite)
               ::json-rpc/call [{:method "wallet_addFavourite"
                                 :params [new-favourite]
                                 :on-success #()}]}
              (navigation/navigate-back))))
