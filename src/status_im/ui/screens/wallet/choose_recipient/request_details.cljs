(ns status-im.ui.screens.wallet.choose-recipient.request-details
  (:require [status-im.utils.money :as money]
            [status-im.ethereum.core :as ethereum]
            [status-im.contact.db :as contact.db]))

(defn- find-address-name [db address]
  (:name (contact.db/find-contact-by-address (:contacts/contacts db) address)))

(defn fill-request-details [db {:keys [address name value symbol gas gasPrice public-key from-chat?]} request?]
  {:pre [(not (nil? address))]}
  (let [name (or name (find-address-name db address))
        data-path (if request?
                    [:wallet :request-transaction]
                    [:wallet :send-transaction])]
    (update-in db data-path
               (fn [{old-symbol :symbol :as old-transaction}]
                 (let [symbol-changed? (not= old-symbol symbol)]
                   (cond-> (assoc old-transaction :to address :to-name name :public-key public-key)
                     value (assoc :amount value)
                     symbol (assoc :symbol symbol)
                     (and gas symbol-changed?) (assoc :gas (money/bignumber gas))
                     from-chat? (assoc :from-chat? from-chat?)
                     (and gasPrice symbol-changed?)
                     (assoc :gas-price (money/bignumber gasPrice))
                     (and symbol (not gasPrice) symbol-changed?)
                     (assoc :gas-price (ethereum/estimate-gas symbol))))))))
