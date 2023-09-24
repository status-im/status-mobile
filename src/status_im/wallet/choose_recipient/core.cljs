(ns status-im.wallet.choose-recipient.core
  (:require [re-frame.core :as re-frame]
            [status-im.bottom-sheet.events :as bottom-sheet]
            [status-im.contact.db :as contact.db]
            [utils.ethereum.chain :as chain]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.ethereum.eip681 :as eip681]
            [status-im.ethereum.ens :as ens]
            [utils.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scaner]
            [status-im.router.core :as router]
            [utils.re-frame :as rf]
            [utils.url :as url]
            [utils.money :as money]
            [status-im.utils.universal-links.utils :as links]
            [status-im2.navigation.events :as navigation]
            [clojure.string :as string]))

;; FIXME(Ferossgp): Should be part of QR scanner not wallet
(rf/defn toggle-flashlight
  {:events [:wallet-legacy/toggle-flashlight]}
  [{:keys [db]}]
  (let [flashlight-state (get-in db [:wallet-legacy :send-transaction :camera-flashlight])
        toggled-state    (if (= :on flashlight-state) :off :on)]
    {:db (assoc-in db [:wallet-legacy :send-transaction :camera-flashlight] toggled-state)}))

(defn- find-address-name
  [db address]
  (:name (contact.db/find-contact-by-address (:contacts/contacts db) address)))

(rf/defn set-recipient
  {:events [:wallet-legacy.send/set-recipient]}
  [{:keys [db]} address]
  {:db       (-> db
                 (dissoc :wallet-legacy/recipient)
                 (assoc-in [:ui/search :recipient-filter] nil)
                 (assoc-in [:wallet-legacy/prepare-transaction :to] address))
   :dispatch [:navigate-back]})

(re-frame/reg-fx
 ::resolve-addresses
 (fn [{:keys [chain-id ens-names callback]}]
   ;; resolve all addresses then call the callback function with the array of
   ;;addresses as parameter
   (-> (js/Promise.all
        (clj->js (mapv (fn [ens-name]
                         (js/Promise.
                          (fn [resolve-fn _]
                            (ens/address chain-id ens-name resolve-fn))))
                       ens-names)))
       (.then callback)
       (.catch (fn [error]
                 (js/console.log error))))))

(defn- fill-prepare-transaction-details
  [db
   {address   :address
    name      :name
    value     :value
    sym       :symbol
    gas       :gas
    gas-price :gasPrice
    gas-limit :gasLimit
    :or       {sym :ETH}}
   all-tokens]
  (assoc db
         :wallet-legacy/prepare-transaction
         (cond-> {:to      address
                  :to-name (or name (find-address-name db address))
                  :from    (wallet.utils/get-default-account
                            (get db :profile/wallet-accounts))}
           gas       (assoc :gas (money/bignumber gas))
           gas-limit (assoc :gas (money/bignumber gas-limit))
           gas-price (assoc :gasPrice (money/bignumber gas-price))
           value     (assoc :amount-text
                            (if (= :ETH sym)
                              (str (money/internal->formatted value sym (get all-tokens sym)))
                              (str value)))
           sym       (assoc :symbol sym))))

(rf/defn request-uri-parsed
  {:events [:wallet-legacy/request-uri-parsed]}
  [{{:networks/keys      [networks current-network]
     :wallet-legacy/keys [all-tokens]
     :as                 db}
    :db}
   {:keys [chain-id] :as data}
   uri]
  (let [{:keys [address gasPrice] :as details}
        (eip681/extract-request-details data all-tokens)]
    (if address
      (if (:wallet-legacy/recipient db)
        {:db (update db
                     :wallet-legacy/recipient assoc
                     :resolved-address        address
                     :address                 address)}
        (if (:wallet-legacy/prepare-transaction db)
          {:db (update db
                       :wallet-legacy/prepare-transaction assoc
                       :to                                address
                       :to-name                           (find-address-name db address))}
          (let [current-chain-id (get-in networks [current-network :config :NetworkId])]
            (merge {:db       (fill-prepare-transaction-details db details all-tokens)
                    :dispatch [:open-modal :prepare-send-transaction]}
                   (when-not gasPrice
                     {:signing/update-gas-price
                      {:success-callback
                       #(re-frame/dispatch
                         [:wallet-legacy.send/update-gas-price-success :wallet-legacy/prepare-transaction
                          %])
                       :network-id (get-in (chain/current-network db)
                                           [:config :NetworkId])}})
                   (when (and chain-id (not= current-chain-id chain-id))
                     {:ui/show-error (i18n/label :t/wallet-invalid-chain-id
                                                 {:data uri :chain current-chain-id})})))))
      {:ui/show-error (i18n/label :t/wallet-invalid-address {:data uri})})))

(rf/defn qr-scanner-allowed
  {:events [:wallet-legacy.send/qr-scanner]}
  [{:keys [db] :as cofx} options]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet-old)
            (qr-scaner/scan-qr-code options)))

(rf/defn parse-eip681-uri-and-resolve-ens
  {:events [:wallet-legacy/parse-eip681-uri-and-resolve-ens]}
  [{db :db :as cofx} {:keys [message uri paths ens-names error]} ignore-url]
  (if-not error
    ;; first we get a vector of ens-names to resolve and a vector of paths of these names
    (if (empty? ens-names)
      ;; if there are no ens-names, we dispatch request-uri-parsed immediately
      (request-uri-parsed cofx message uri)
      {::resolve-addresses
       {:chain-id (chain/chain-id db)
        :ens-names ens-names
        :callback
        (fn [addresses]
          (re-frame/dispatch
           [:wallet-legacy/request-uri-parsed
            ;; we replace ens-names at their path in the message by their actual address
            (reduce (fn [message [path address]]
                      (assoc-in message path address))
                    message
                    (map vector paths addresses)) uri]))}})
    (if (and (url/url? uri) (not ignore-url))
      (if (links/universal-link? uri)
        {:dispatch [:universal-links/handle-url uri]}
        {:browser/show-browser-selection uri})
      (if (string/starts-with? uri "wc:")
        {:ui/show-error "Wallet Connect not implemented"}
        ;; Re-enable with https://github.com/status-im/status-mobile/issues/13429
        ;; {:dispatch [::qr-scaner/handle-wallet-connect-uri {:data uri}]}
        {:ui/show-error (i18n/label :t/wallet-invalid-address {:data uri})}))))

(rf/defn qr-scanner-result
  {:events [:wallet-legacy.send/qr-scanner-result]}
  [cofx data {:keys [ignore-url]}]
  (rf/merge cofx
            (navigation/navigate-back)
            (parse-eip681-uri-and-resolve-ens (router/match-eip681 data) ignore-url)))
