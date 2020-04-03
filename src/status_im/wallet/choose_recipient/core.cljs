(ns status-im.wallet.choose-recipient.core
  (:require [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [status-im.i18n :as i18n]
            [status-im.utils.money :as money]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.navigation :as navigation]
            [clojure.string :as string]
            [status-im.ethereum.stateofus :as stateofus]))

(fx/defn toggle-flashlight
  {:events [:wallet/toggle-flashlight]}
  [{:keys [db]}]
  (let [flashlight-state (get-in db [:wallet :send-transaction :camera-flashlight])
        toggled-state (if (= :on flashlight-state) :off :on)]
    {:db (assoc-in db [:wallet :send-transaction :camera-flashlight] toggled-state)}))

(defn- find-address-name [db address]
  (:name (contact.db/find-contact-by-address (:contacts/contacts db) address)))

(defn use-default-eth-gas [fx]
  (assoc-in fx [:db :wallet :send-transaction :gas]
            ethereum/default-transaction-gas))

(re-frame/reg-fx
 ::resolve-address
 (fn [{:keys [registry ens-name cb]}]
   (ens/get-addr registry ens-name cb)))

(re-frame/reg-fx
 ::resolve-addresses
 (fn [{:keys [registry ens-names callback]}]
   ;; resolve all addresses then call the callback function with the array of
   ;;addresses as parameter
   (-> (js/Promise.all
        (clj->js (mapv (fn [ens-name]
                         (js/Promise.
                          (fn [resolve reject]
                            (ens/get-addr registry ens-name resolve))))
                       ens-names)))
       (.then callback)
       (.catch (fn [error]
                 (js/console.log error))))))

(fx/defn set-recipient
  {:events [:wallet.send/set-recipient ::recipient-address-resolved]}
  [{:keys [db]} raw-recipient]
  (let [chain (ethereum/chain-keyword db)
        recipient (when raw-recipient (string/trim raw-recipient))]
    (cond
      (ethereum/address? recipient)
      (let [checksum (eip55/address->checksum recipient)]
        (if (eip55/valid-address-checksum? checksum)
          {:db       (-> db
                         (assoc-in [:wallet/prepare-transaction :to] checksum)
                         (assoc-in [:wallet/prepare-transaction :modal-opened?] false))
           :dispatch [:navigate-back]}
          {:ui/show-error (i18n/label :t/wallet-invalid-address-checksum {:data recipient})}))
      (not (string/blank? recipient))
      {::resolve-address {:registry (get ens/ens-registries chain)
                          :ens-name (if (= (.indexOf recipient ".") -1)
                                      (stateofus/subdomain recipient)
                                      recipient)
                          :cb       #(re-frame/dispatch [::recipient-address-resolved %])}}
      :else
      {:ui/show-error (i18n/label :t/wallet-invalid-address {:data recipient})})))

(defn- fill-prepare-transaction-details
  [db
   {:keys [address name value symbol gas gasPrice gasLimit]
    :or   {symbol :ETH}}
   all-tokens]
  (assoc db :wallet/prepare-transaction
         (cond-> {:to      address
                  :to-name (or name (find-address-name db address))
                  :from    (ethereum/get-default-account
                            (get db :multiaccount/accounts))}
           gas (assoc :gas (money/bignumber gas))
           gasLimit  (assoc :gas (money/bignumber gasLimit))
           gasPrice (assoc :gasPrice (money/bignumber gasPrice))
           value (assoc :amount-text
                        (if (= :ETH symbol)
                          (str (money/internal->formatted value symbol (get all-tokens symbol)))
                          (str value)))
           symbol (assoc :symbol symbol))))

(fx/defn request-uri-parsed
  {:events [:wallet/request-uri-parsed]}
  [{{:networks/keys [networks current-network]
     :wallet/keys   [all-tokens] :as db} :db}
   {:keys [chain-id] :as data}
   uri]
  (let [{:keys [address] :as details}
        (eip681/extract-request-details data all-tokens)]
    (if address
      (if (:wallet/prepare-transaction db)
        {:db (update db :wallet/prepare-transaction assoc
                     :to address :to-name (find-address-name db address))}
        (let [current-chain-id (get-in networks [current-network :config :NetworkId])]
          (merge {:db (fill-prepare-transaction-details db details all-tokens)}
                 (when (and chain-id (not= current-chain-id chain-id))
                   {:ui/show-error (i18n/label :t/wallet-invalid-chain-id
                                               {:data uri :chain current-chain-id})}))))
      {:ui/show-error (i18n/label :t/wallet-invalid-address {:data uri})})))

(fx/defn qr-scanner-cancel
  {:events [:wallet.send/qr-scanner-cancel]}
  [{db :db} _]
  {:db (assoc-in db [:wallet/prepare-transaction :modal-opened?] false)})

(fx/defn parse-eip681-uri-and-resolve-ens
  [{db :db :as cofx} uri]
  (if-let [message (eip681/parse-uri uri)]
    ;; first we get a vector of ens-names to resolve and a vector of paths of
    ;; these names
    (let [{:keys [paths ens-names]}
          (reduce (fn [acc path]
                    (let [address (get-in message path)]
                      (if (ens/is-valid-eth-name? address)
                        (-> acc
                            (update :paths conj path)
                            (update :ens-names conj address))
                        acc)))
                  {:paths [] :ens-names []}
                  [[:address] [:function-arguments :address]])]
      (println "message" message)
      (if (empty? ens-names)
        ;; if there are no ens-names, we dispatch request-uri-parsed immediately
        (request-uri-parsed cofx message uri)
        {::resolve-addresses
         {:registry (get ens/ens-registries (ethereum/chain-keyword db))
          :ens-names ens-names
          :callback
          (fn [addresses]
            (re-frame/dispatch
             [:wallet/request-uri-parsed
              ;; we replace ens-names at their path in the message by their
              ;; actual address
              (reduce (fn [message [path address]]
                        (assoc-in message path address))
                      message
                      (map vector paths addresses)) uri]))}}))
    {:ui/show-error (i18n/label :t/wallet-invalid-address {:data uri})}))

(fx/defn qr-scanner-result
  {:events [:wallet.send/qr-scanner-result]}
  [{db :db :as cofx} data opts]
  (fx/merge cofx
            (navigation/navigate-back)
            (parse-eip681-uri-and-resolve-ens data)
            (fn [{:keys [db]}]
              (when (get-in db [:wallet/prepare-transaction :modal-opened?])
                {:db (assoc-in db [:wallet/prepare-transaction :modal-opened?] false)}))))
