(ns status-im.wallet.choose-recipient.core
  (:require [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.money :as money]
            [status-im.utils.fx :as fx]
            [status-im.router.core :as router]
            [status-im.qr-scanner.core :as qr-scaner]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.navigation :as navigation]))

;; FIXME(Ferossgp): Should be part of QR scanner not wallet
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

(fx/defn set-recipient
  {:events [:wallet.send/set-recipient]}
  [{:keys [db]} address]
  {:db       (-> db
                 (dissoc :wallet/recipient)
                 (assoc-in [:ui/search :recipient-filter] nil)
                 (assoc-in [:wallet/prepare-transaction :to] address))
   :dispatch [:navigate-back]})

(re-frame/reg-fx
 ::resolve-addresses
 (fn [{:keys [registry ens-names callback]}]
   ;; resolve all addresses then call the callback function with the array of
   ;;addresses as parameter
   (-> (js/Promise.all
        (clj->js (mapv (fn [ens-name]
                         (js/Promise.
                          (fn [resolve _]
                            (ens/get-addr registry ens-name resolve))))
                       ens-names)))
       (.then callback)
       (.catch (fn [error]
                 (js/console.log error))))))

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
      (if (:wallet/recipient db)
        {:db (update db :wallet/recipient assoc :resolved-address address
                     :address address)}
        (if (:wallet/prepare-transaction db)
          {:db (update db :wallet/prepare-transaction assoc
                       :to address :to-name (find-address-name db address))}
          (let [current-chain-id (get-in networks [current-network :config :NetworkId])]
            (merge {:db (fill-prepare-transaction-details db details all-tokens)
                    :dispatch [:navigate-to :prepare-send-transaction]}
                   (when (and chain-id (not= current-chain-id chain-id))
                     {:ui/show-error (i18n/label :t/wallet-invalid-chain-id
                                                 {:data uri :chain current-chain-id})})))))
      {:ui/show-error (i18n/label :t/wallet-invalid-address {:data uri})})))

(fx/defn qr-scanner-allowed
  {:events [:wallet.send/qr-scanner]}
  [{:keys [db] :as cofx} options]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (qr-scaner/scan-qr-code options)))

(fx/defn parse-eip681-uri-and-resolve-ens
  {:events [:wallet/parse-eip681-uri-and-resolve-ens]}
  [{db :db :as cofx} {:keys [message uri paths ens-names error]}]
  (if-not error
    ;; first we get a vector of ens-names to resolve and a vector of paths of
    ;; these names
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
                    (map vector paths addresses)) uri]))}})
    {:ui/show-error (i18n/label :t/wallet-invalid-address {:data uri})}))

(fx/defn qr-scanner-result
  {:events [:wallet.send/qr-scanner-result]}
  [cofx data _]
  (fx/merge cofx
            (navigation/navigate-back)
            (parse-eip681-uri-and-resolve-ens (router/match-eip681 data))))
