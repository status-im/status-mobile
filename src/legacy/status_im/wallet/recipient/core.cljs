(ns legacy.status-im.wallet.recipient.core
  (:require
    [clojure.string :as string]
    [legacy.status-im.ethereum.ens :as ens]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.utils.random :as random]
    [legacy.status-im.utils.utils :as utils]
    [re-frame.core :as re-frame]
    [status-im2.common.json-rpc.events :as json-rpc]
    [status-im2.navigation.events :as navigation]
    [utils.address :as address]
    [utils.ens.core :as utils.ens]
    [utils.ens.stateofus :as stateofus]
    [utils.ethereum.chain :as chain]
    [utils.ethereum.eip.eip55 :as eip55]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.string :as utils.string]))

;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(re-frame/reg-fx
 ::resolve-address
 (fn [{:keys [chain-id ens-name cb]}]
   (ens/address chain-id ens-name cb)))

(re-frame/reg-fx
 :wallet-legacy.recipient/address-paste
 (fn []
   (react/get-from-clipboard
    #(re-frame/dispatch [:wallet-legacy.recipient/address-changed (string/trim %)]))))

(rf/defn address-paste-pressed
  {:events [:wallet-legacy.recipient/address-paste-pressed]}
  [_]
  {:wallet-legacy.recipient/address-paste nil})

(rf/defn set-recipient
  {:events [::recipient-address-resolved]}
  [{:keys [db] :as cofx} raw-recipient id]
  (when (or (not id) (= id @resolve-last-id))
    (reset! resolve-last-id nil)
    (let [recipient (utils.string/safe-trim raw-recipient)]
      (cond
        (address/address? recipient)
        (let [checksum (eip55/address->checksum recipient)]
          (if (eip55/valid-address-checksum? checksum)
            (rf/merge cofx
                      {:db (-> db
                               (assoc-in [:wallet-legacy/recipient :searching] false)
                               (assoc-in [:wallet-legacy/recipient :resolved-address] checksum))}
                      (json-rpc/call
                       {:method            "eth_getCode"
                        :params            [checksum "latest"]
                        :on-success        #(when (not= "0x" %)
                                              (utils/show-popup (i18n/label :t/warning)
                                                                (i18n/label
                                                                 :t/warning-sending-to-contract-descr)))
                        :number-of-retries 3}))
            {:ui/show-error (i18n/label :t/wallet-invalid-address-checksum {:data recipient})
             :db            (assoc-in db [:wallet-legacy/recipient :searching] false)}))
        (and (not (string/blank? recipient))
             (not (string/starts-with? recipient "0x"))
             (utils.ens/valid-eth-name-prefix? recipient))
        (let [ens-name (if (= (.indexOf ^js recipient ".") -1)
                         (stateofus/subdomain recipient)
                         recipient)]
          (if (utils.ens/is-valid-eth-name? ens-name)
            (do
              (reset! resolve-last-id (random/id))
              {::resolve-address
               {:chain-id (chain/chain-id db)
                :ens-name ens-name
                :cb       #(re-frame/dispatch [::recipient-address-resolved % @resolve-last-id])}})
            {:db (assoc-in db [:wallet-legacy/recipient :searching] false)}))
        :else
        {:db (assoc-in db [:wallet-legacy/recipient :searching] false)}))))

(rf/defn address-changed
  {:events [:wallet-legacy.recipient/address-changed]}
  [{:keys [db] :as cofx} new-identity]
  (rf/merge cofx
            {:db (update db
                         :wallet-legacy/recipient assoc
                         :address                 new-identity
                         :resolved-address        nil
                         :searching               true)}
            (set-recipient new-identity nil)))

(rf/defn recipient-modal-closed
  {:events [:wallet-legacy/recipient-modal-closed]}
  [{:keys [db]}]
  {:db (dissoc db :wallet-legacy/recipient)})

(rf/defn add-favourite
  {:events [:wallet-legacy/add-favourite]}
  [{:keys [db] :as cofx} address name]
  (let [new-favourite {:address   address
                       :name      (or name "")
                       :favourite true}]
    (rf/merge cofx
              {:db            (assoc-in db [:wallet-legacy/favourites address] new-favourite)
               :json-rpc/call [{:method     "wallet_addSavedAddress"
                                :params     [new-favourite]
                                :on-success #()}]}
              (navigation/navigate-back))))
