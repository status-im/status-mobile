(ns status-im.wallet.custom-tokens.core
  (:require [clojure.string :as string]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [utils.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]
            [status-im.wallet.core :as wallet]
            [status-im.wallet.prices :as prices]
            [status-im2.navigation.events :as navigation]))

(re-frame/reg-fx
 :wallet.custom-token/contract-address-paste
 (fn []
   (react/get-from-clipboard
    #(re-frame/dispatch [:wallet.custom-token/contract-address-is-pasted (string/trim %)]))))

(defn field-exists?
  [{:wallet/keys [all-tokens]} field-key field-value]
  (some #(= field-value (get % field-key))
        (vals all-tokens)))

(defn token-in-list?
  [{:wallet/keys [all-tokens]} contract]
  (not (nil? (get all-tokens (string/lower-case contract)))))

(rf/defn contract-address-is-changed
  {:events [:wallet.custom-token/contract-address-is-pasted]}
  [{:keys [db]} contract]
  (if (ethereum/address? contract)
    (if (token-in-list? db contract)
      {:db (assoc db
                  :wallet/custom-token-screen
                  {:contract contract :error (i18n/label :t/already-have-asset)})}
      {:db            (assoc db
                             :wallet/custom-token-screen
                             {:contract contract :in-progress? true})
       :json-rpc/call [{:method     "wallet_discoverToken"
                        :params     [(ethereum/chain-id db) contract]
                        :on-success #(re-frame/dispatch [:wallet.custom-token/token-discover-result %])
                        :on-error   #(re-frame/dispatch [:wallet.custom-token/not-supported])}]})
    {:db (assoc db
                :wallet/custom-token-screen
                {:contract contract
                 :error    (i18n/label :t/wrong-address)})}))

(rf/defn token-discover-result
  {:events [:wallet.custom-token/token-discover-result]}
  [{:keys [db]} {:keys [name symbol decimals]}]
  (let [symbol-exists? (field-exists? db :symbol (keyword symbol))]
    {:db (update db
                 :wallet/custom-token-screen
                 merge
                 {:name         name
                  :symbol       symbol
                  :error-symbol (when symbol-exists?
                                  (i18n/label :t/you-already-have-an-asset {:value symbol}))
                  :decimals     (str decimals)
                  :in-progress? nil})}))

(rf/defn not-supported
  {:events [:wallet.custom-token/not-supported]}
  [{:keys [db]}]
  {:db               (assoc-in db [:wallet/custom-token-screen :in-progress?] nil)
   :utils/show-popup {:content (i18n/label :t/contract-isnt-supported)}})

(rf/defn add-custom-token
  {:events [:wallet.custom-token.ui/add-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [contract name symbol decimals]} (get db :wallet/custom-token-screen)
        symbol                                  (keyword symbol)
        new-token                               {:address  contract
                                                 :name     name
                                                 :symbol   symbol
                                                 :decimals (int decimals)
                                                 :color    (rand-nth colors/chat-colors)}]
    (rf/merge cofx
              {:db            (assoc-in db
                               [:wallet/all-tokens contract]
                               (assoc new-token :custom? true))
               :json-rpc/call [{:method     "wallet_addCustomToken"
                                :params     [new-token]
                                :on-success #()}]}
              (wallet/add-custom-token new-token)
              (prices/update-prices)
              ((navigation/navigate-back nil)))))

(rf/defn remove-custom-token
  {:events [:wallet.custom-token.ui/remove-pressed]}
  [{:keys [db] :as cofx} {:keys [address] :as token} navigate-back?]
  (rf/merge cofx
            {:db            (update db :wallet/all-tokens dissoc address)
             :json-rpc/call [{:method     "wallet_deleteCustomToken"
                              :params     [address]
                              :on-success #()}]}
            (wallet/remove-custom-token token)
            (when navigate-back?
              ((navigation/navigate-back nil)))))

(rf/defn field-is-edited
  {:events [:wallet.custom-token.ui/field-is-edited]}
  [{:keys [db] :as cofx} field-key value]
  (case field-key
    :contract (contract-address-is-changed cofx value)
    :name     {:db (update db
                           :wallet/custom-token-screen
                           merge
                           {field-key
                            value
                            :error-name
                            (when (field-exists? db field-key value)
                              (i18n/label :t/you-already-have-an-asset
                                          {:value value}))})}
    :symbol   {:db (update db
                           :wallet/custom-token-screen
                           merge
                           {field-key
                            value
                            :error-symbol
                            (when (field-exists? db field-key (keyword value))
                              (i18n/label :t/you-already-have-an-asset {:value value}))})}
    :decimals {:db (assoc-in db
                    [:wallet/custom-token-screen :decimals]
                    value)}))
