(ns status-im.wallet.custom-tokens.core
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.utils.ethereum.core :as ethereum]
            [clojure.string :as string]
            [status-im.ethereum.decode :as decode]
            [status-im.utils.fx :as fx]
            [status-im.ui.screens.wallet.settings.models :as models]))

(re-frame/reg-fx
 :wallet.custom-token/get-decimals
 (fn [contract]
   (ethereum/call
    (ethereum/call-params contract "decimals()")
    #(re-frame/dispatch [:wallet.custom-token/decimals-result %]))))

(re-frame/reg-fx
 :wallet.custom-token/get-symbol
 (fn [contract]
   (ethereum/call
    (ethereum/call-params contract "symbol()")
    #(re-frame/dispatch [:wallet.custom-token/symbol-result contract %]))))

(re-frame/reg-fx
 :wallet.custom-token/get-balance
 (fn [[contract address]]
   (ethereum/call
    (ethereum/call-params contract "balanceOf(address)" (ethereum/normalized-address address))
    #(re-frame/dispatch [:wallet.custom-token/balance-result contract %]))))

(re-frame/reg-fx
 :wallet.custom-token/get-name
 (fn [contract]
   (ethereum/call
    (ethereum/call-params contract "name()")
    #(re-frame/dispatch [:wallet.custom-token/name-result contract %]))))

(re-frame/reg-fx
 :wallet.custom-token/get-total-supply
 (fn [contract]
   (ethereum/call
    (ethereum/call-params contract "totalSupply()")
    #(re-frame/dispatch [:wallet.custom-token/total-supply-result contract %]))))

(re-frame/reg-fx
 :wallet.custom-token/contract-address-paste
 (fn []
   (react/get-from-clipboard #(re-frame/dispatch [:wallet.custom-token/contract-address-is-pasted (string/trim %)]))))

(defn field-exists? [{:wallet/keys [all-tokens] :as db} field-key field-value]
  (let [chain-key (ethereum/get-chain-keyword db)]
    (some #(= field-value (get % field-key)) (vals (get all-tokens chain-key)))))

(fx/defn total-supply-result [{:keys [db]} contract result]
  (if (and (string? result) (string/starts-with? result "0x") (> (count result) 2))
    {:wallet.custom-token/get-balance [contract (get-in db [:account/account :address])]}
    {:db (update db :wallet/custom-token-screen merge {:in-progress? nil :error (i18n/label :t/wrong-contract)})}))

(defn token-in-list? [{:wallet/keys [all-tokens] :as db} contract]
  (let [chain-key (ethereum/get-chain-keyword db)
        addresses (set (map string/lower-case (keys (get all-tokens chain-key))))]
    (not (nil? (get addresses (string/lower-case contract))))))

(fx/defn contract-address-is-changed [{:keys [db]} contract]
  (if (ethereum/address? contract)
    (if (token-in-list? db contract)
      {:db (assoc db :wallet/custom-token-screen {:contract contract :error (i18n/label :t/already-have-asset)})}
      {:db                                   (assoc db :wallet/custom-token-screen {:contract contract :in-progress? true})
       :wallet.custom-token/get-total-supply contract})
    {:db (assoc db :wallet/custom-token-screen {:contract contract :error (i18n/label :t/wrong-address)})}))

(fx/defn decimals-result [{:keys [db]} result]
  {:db (update db :wallet/custom-token-screen merge {:decimals     (str (decode/uint result))
                                                     :in-progress? nil})})

(fx/defn symbol-result [{:keys [db]} contract result]
  (let [token-symbol   (decode/string result)
        symbol-exists? (field-exists? db :symbol (keyword token-symbol))]
    {:db
     (update db :wallet/custom-token-screen merge
             {:symbol       token-symbol
              :error-symbol (when symbol-exists?
                              (i18n/label :t/you-already-have-an-asset {:value token-symbol}))})
     :wallet.custom-token/get-decimals
     contract}))

(fx/defn name-result [{:keys [db]} contract result]
  (let [token-name   (decode/string result)
        name-exists? (field-exists? db :name token-name)]
    {:db
     (update db :wallet/custom-token-screen merge
             {:name       token-name
              :error-name (when name-exists?
                            (i18n/label :t/you-already-have-an-asset {:value token-name}))})
     :wallet.custom-token/get-symbol
     contract}))

(fx/defn balance-result [{:keys [db]} contract result]
  (if (and (string? result) (string/starts-with? result "0x") (> (count result) 2))
    {:db                           (assoc-in db [:wallet/custom-token-screen :balance] (str (decode/uint result)))
     :wallet.custom-token/get-name contract}
    {:db (update db :wallet/custom-token-screen merge {:in-progress? nil :error (i18n/label :t/wrong-contract)})}))

(fx/defn add-pressed [{:keys [db] :as cofx}]
  (let [{:keys [contract name symbol decimals]} (get db :wallet/custom-token-screen)
        chain-key (ethereum/get-chain-keyword db)
        symbol    (keyword symbol)
        new-token {:address  contract :name name :symbol symbol :custom? true
                   :decimals (int decimals) :color (rand-nth colors/chat-colors)}]
    (fx/merge (assoc-in cofx [:db :wallet/all-tokens chain-key contract] new-token)
              (models/add-custom-token new-token))))

(fx/defn field-is-edited [{:keys [db] :as cofx} field-key value]
  (case field-key
    :contract (contract-address-is-changed cofx value)
    :name {:db (update db :wallet/custom-token-screen merge
                       {field-key
                        value
                        :error-name
                        (when (field-exists? db field-key value)
                          (i18n/label :t/you-already-have-an-asset {:value value}))})}
    :symbol {:db (update db :wallet/custom-token-screen merge
                         {field-key
                          value
                          :error-symbol
                          (when (field-exists? db field-key (keyword value))
                            (i18n/label :t/you-already-have-an-asset {:value value}))})}
    :decimals {:db (assoc-in db [:wallet/custom-token-screen :decimals] value)}))