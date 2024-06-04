(ns status-im.contexts.wallet.wallet-connect.events
  (:require [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            status-im.contexts.wallet.wallet-connect.effects
            status-im.contexts.wallet.wallet-connect.processing-events
            status-im.contexts.wallet.wallet-connect.responding-events
            [taoensso.timbre :as log]
            [utils.ethereum.chain :as chain]))

(rf/reg-event-fx
 :wallet-connect/init
 (fn []
   {:fx [[:effects.wallet-connect/init
          {:on-success #(rf/dispatch [:wallet-connect/on-init-success %])
           :on-fail    #(rf/dispatch [:wallet-connect/on-init-fail %])}]]}))

(rf/reg-event-fx
 :wallet-connect/on-init-success
 (fn [{:keys [db]} [web3-wallet]]
   {:db (assoc db :wallet-connect/web3-wallet web3-wallet)
    :fx [[:dispatch [:wallet-connect/register-event-listeners]]
         [:effects.wallet-connect/fetch-pairings
          {:web3-wallet web3-wallet
           :on-fail     #(log/error "Failed to get dApp pairings" {:error %})
           :on-success  (fn [data]
                          (rf/dispatch [:wallet-connect/set-pairings
                                        (js->clj data :keywordize-keys true)]))}]]}))

(rf/reg-event-fx
 :wallet-connect/register-event-listeners
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-proposal-event
             #(rf/dispatch [:wallet-connect/on-session-proposal %])]]
           [:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-request-event
             #(rf/dispatch [:wallet-connect/on-session-request %])]]]})))

(rf/reg-event-fx
 :wallet-connect/on-init-fail
 (fn [_ [error]]
   (log/error "Failed to initialize Wallet Connect"
              {:error error
               :event :wallet-connect/on-init-fail})))

(rf/reg-event-fx
 :wallet-connect/on-session-proposal
 (fn [{:keys [db]} [proposal]]
   (log/info "Received Wallet Connect session proposal: " {:id (:id proposal)})
   {:db (assoc db :wallet-connect/current-proposal proposal)
    :fx [[:dispatch
          [:open-modal :screen/wallet.wallet-connect-session-proposal]]]}))

(rf/reg-event-fx
 :wallet-connect/on-session-request
 (fn [_ [event]]
   (log/info "Received Wallet Connect session request: " event)
   {:fx [[:dispatch [:wallet-connect/process-session-request event]]]}))

(rf/reg-event-fx
 :wallet-connect/reset-current-session-proposal
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet-connect/current-proposal)}))

(rf/reg-event-fx
 :wallet-connect/reset-current-session-request
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet-connect/current-request)}))

(rf/reg-event-fx
 :wallet-connect/set-pairings
 (fn [{:keys [db]} [pairings]]
   {:db (assoc db :wallet-connect/pairings pairings)}))

(rf/reg-event-fx
 :wallet-connect/remove-pairing-by-topic
 (fn [{:keys [db]} [topic]]
   {:db (update db
                :wallet-connect/pairings
                (fn [pairings]
                  (remove #(= (:topic %) topic) pairings)))}))

(rf/reg-event-fx
 :wallet-connect/disconnect-dapp
 (fn [{:keys [db]} [{:keys [topic on-success on-fail]}]]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/disconnect
            {:web3-wallet web3-wallet
             :topic       topic
             :on-fail     on-fail
             :on-success  on-success}]]})))

(rf/reg-event-fx
 :wallet-connect/pair
 (fn [{:keys [db]} [url]]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/pair
            {:web3-wallet web3-wallet
             :url         url
             :on-fail     #(log/error "Failed to pair with dApp" {:error %})
             :on-success  #(log/info "dApp paired successfully")}]]})))

(rf/reg-event-fx
 :wallet-connect/close-session-request
 (fn [_ _]
   {:fx [[:dispatch [:navigate-back]]
         [:dispatch [:wallet-connect/reset-current-session-request]]]}))

(rf/reg-event-fx
 :wallet-connect/fetch-active-sessions
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/fetch-active-sessions
            {:web3-wallet web3-wallet
             :on-fail     #(log/error "Failed to get active sessions" {:error %})
             :on-success  #(log/info "Got active sessions successfully" {:sessions %})}]]})))

(rf/reg-event-fx
 :wallet-connect/approve-session
 (fn [{:keys [db]}]
   (let [web3-wallet          (get db :wallet-connect/web3-wallet)
         current-proposal     (get db :wallet-connect/current-proposal)
         accounts             (get-in db [:wallet :accounts])
         supported-chain-ids  (->> db
                                   chain/chain-ids
                                   (map wallet-connect-core/chain-id->eip155)
                                   vec)
         ;; NOTE: for now using the first account, but should be using the account selected by the
         ;; user on the connection screen. The default would depend on where the connection started
         ;; from:
         ;; - global scanner -> first account in list
         ;; - wallet account dapps -> account that is selected
         address              (-> accounts keys first)
         accounts             (-> (partial wallet-connect-core/format-eip155-address address)
                                  (map supported-chain-ids))
         supported-namespaces (clj->js {:eip155
                                        {:chains   supported-chain-ids
                                         :methods  constants/wallet-connect-supported-methods
                                         :events   constants/wallet-connect-supported-events
                                         :accounts accounts}})]
     {:fx [[:effects.wallet-connect/approve-session
            {:web3-wallet          web3-wallet
             :proposal             current-proposal
             :supported-namespaces supported-namespaces
             :on-success           (fn []
                                     (log/info "Wallet Connect session approved")
                                     (rf/dispatch [:wallet-connect/reset-current-session-proposal]))
             :on-fail              (fn [error]
                                     (log/error "Wallet Connect session approval failed"
                                                {:error error
                                                 :event :wallet-connect/approve-session})
                                     (rf/dispatch
                                      [:wallet-connect/reset-current-session-proposal]))}]]})))
