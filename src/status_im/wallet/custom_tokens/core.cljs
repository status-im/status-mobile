(ns status-im.wallet.custom-tokens.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]
            [status-im.navigation :as navigation]
            [status-im.wallet.prices :as prices]))

(re-frame/reg-fx
 :wallet.custom-token/get-decimals
 (fn [contract]
   (json-rpc/eth-call
    {:contract contract
     :method "decimals()"
     :outputs ["uint256"]
     :on-success
     (fn [[contract-decimals]]
       (re-frame/dispatch [:wallet.custom-token/decimals-result
                           contract-decimals]))})))

(re-frame/reg-fx
 :wallet.custom-token/get-symbol
 (fn [contract]
   (json-rpc/eth-call
    {:contract contract
     :method "symbol()"
     :outputs ["string"]
     :on-success
     (fn [[contract-symbol]]
       (re-frame/dispatch [:wallet.custom-token/symbol-result
                           contract
                           contract-symbol]))})))

(re-frame/reg-fx
 :wallet.custom-token/get-balance
 (fn [[contract wallet-address]]
   (json-rpc/eth-call
    {:contract   contract
     :method     "balanceOf(address)"
     :params     [wallet-address]
     :outputs    ["uint256"]
     :on-success
     (fn [[balance]]
       (re-frame/dispatch [:wallet.custom-token/balance-result
                           contract
                           (money/bignumber balance)]))})))

(re-frame/reg-fx
 :wallet.custom-token/get-name
 (fn [contract]
   (json-rpc/eth-call
    {:contract contract
     :method "name()"
     :outputs ["string"]
     :on-success
     (fn [[contract-name]]
       (re-frame/dispatch [:wallet.custom-token/name-result
                           contract
                           contract-name]))})))

(re-frame/reg-fx
 :wallet.custom-token/get-total-supply
 (fn [contract]
   (json-rpc/eth-call
    {:contract contract
     :method "totalSupply()"
     :outputs ["uint256"]
     :on-success
     (fn [[contract-total-supply]]
       (re-frame/dispatch [:wallet.custom-token/total-supply-result
                           contract
                           (money/bignumber contract-total-supply)]))})))

(re-frame/reg-fx
 :wallet.custom-token/contract-address-paste
 (fn []
   (react/get-from-clipboard
    #(re-frame/dispatch [:wallet.custom-token/contract-address-is-pasted
                         (string/trim %)]))))

(defn field-exists?
  [{:wallet/keys [all-tokens]} field-key field-value]
  (some #(= field-value (get % field-key))
        (vals all-tokens)))

(fx/defn total-supply-result
  {:events [:wallet.custom-token/total-supply-result]}
  [{:keys [db]} contract total-supply]
  (if (money/valid? total-supply)
    {:wallet.custom-token/get-name contract}
    {:db (update db
                 :wallet/custom-token-screen
                 merge {:in-progress? nil
                        :error (i18n/label :t/wrong-contract)})}))

(defn token-in-list?
  [{:wallet/keys [all-tokens]} contract]
  (not (nil? (get all-tokens (string/lower-case contract)))))

(fx/defn contract-address-is-changed
  {:events [:wallet.custom-token/contract-address-is-pasted]}
  [{:keys [db]} contract]
  (if (ethereum/address? contract)
    (if (token-in-list? db contract)
      {:db (assoc db
                  :wallet/custom-token-screen
                  {:contract contract :error (i18n/label :t/already-have-asset)})}
      {:db (assoc db
                  :wallet/custom-token-screen
                  {:contract contract :in-progress? true})
       :wallet.custom-token/get-total-supply contract})
    {:db (assoc db
                :wallet/custom-token-screen
                {:contract contract
                 :error (i18n/label :t/wrong-address)})}))

(fx/defn decimals-result
  {:events [:wallet.custom-token/decimals-result]}
  [{:keys [db]} result]
  {:db (update db
               :wallet/custom-token-screen
               merge
               {:decimals     (str result)
                :in-progress? nil})})

(fx/defn symbol-result
  {:events [:wallet.custom-token/symbol-result]}
  [{:keys [db]} contract token-symbol]
  (let [symbol-exists? (field-exists? db :symbol (keyword token-symbol))]
    {:db
     (update db
             :wallet/custom-token-screen merge
             {:symbol       token-symbol
              :error-symbol (when symbol-exists?
                              (i18n/label :t/you-already-have-an-asset {:value token-symbol}))})
     :wallet.custom-token/get-decimals
     contract}))

(fx/defn name-result
  {:events [:wallet.custom-token/name-result]}
  [{:keys [db]} contract token-name]
  (let [name-exists? (field-exists? db :name token-name)]
    {:db
     (update db :wallet/custom-token-screen merge
             {:name       token-name
              :error-name (when name-exists?
                            (i18n/label :t/you-already-have-an-asset
                                        {:value token-name}))})
     :wallet.custom-token/get-symbol contract}))

(fx/defn balance-result
  {:events [:wallet.custom-token/balance-result]}
  [{:keys [db]} contract balance]
  (if (money/valid? balance)
    {:db (assoc-in db
                   [:wallet/custom-token-screen :balance]
                   (str balance))
     :wallet.custom-token/get-name contract}
    {:db (update db
                 :wallet/custom-token-screen
                 merge
                 {:in-progress? nil
                  :error (i18n/label :t/wrong-contract)})}))

(fx/defn add-custom-token
  {:events [:wallet.custom-token.ui/add-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [contract name symbol decimals]} (get db :wallet/custom-token-screen)
        symbol    (keyword symbol)
        new-token {:address  contract
                   :name     name
                   :symbol   symbol
                   :decimals (int decimals)
                   :color    (rand-nth colors/chat-colors)}]
    (fx/merge cofx
              {:db (assoc-in db [:wallet/all-tokens contract]
                             (assoc new-token :custom? true))
               ::json-rpc/call [{:method "wallet_addCustomToken"
                                 :params [new-token]
                                 :on-success #()}]}
              (wallet/add-custom-token new-token)
              (prices/update-prices)
              (navigation/navigate-back))))

(fx/defn remove-custom-token
  {:events [:wallet.custom-token.ui/remove-pressed]}
  [{:keys [db] :as cofx} {:keys [address] :as token} navigate-back?]
  (fx/merge cofx
            {:db (update db :wallet/all-tokens dissoc address)
             ::json-rpc/call [{:method "wallet_deleteCustomToken"
                               :params [address]
                               :on-success #()}]}
            (wallet/remove-custom-token token)
            (when navigate-back?
              (navigation/navigate-back))))

(fx/defn field-is-edited
  {:events [:wallet.custom-token.ui/field-is-edited]}
  [{:keys [db] :as cofx} field-key value]
  (case field-key
    :contract (contract-address-is-changed cofx value)
    :name {:db (update db
                       :wallet/custom-token-screen merge
                       {field-key
                        value
                        :error-name
                        (when (field-exists? db field-key value)
                          (i18n/label :t/you-already-have-an-asset
                                      {:value value}))})}
    :symbol {:db (update db :wallet/custom-token-screen merge
                         {field-key
                          value
                          :error-symbol
                          (when (field-exists? db field-key (keyword value))
                            (i18n/label :t/you-already-have-an-asset {:value value}))})}
    :decimals {:db (assoc-in db
                             [:wallet/custom-token-screen :decimals]
                             value)}))
