(ns status-im.contexts.wallet.wallet-connect.events.core
  (:require [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.constants :as constants]
            status-im.contexts.wallet.wallet-connect.events.effects
            status-im.contexts.wallet.wallet-connect.events.network
            status-im.contexts.wallet.wallet-connect.events.session-proposals
            status-im.contexts.wallet.wallet-connect.events.session-requests
            status-im.contexts.wallet.wallet-connect.events.session-responses
            status-im.contexts.wallet.wallet-connect.events.sessions
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]))

(rf/reg-event-fx
 :wallet-connect/init
 (fn [{:keys [db]}]
   (let [network-status       (:network/status db)
         web3-wallet-missing? (-> db :wallet-connect/web3-wallet boolean not)]
     (if (and (= network-status :online) web3-wallet-missing?)
       (do (log/info "Initialising WalletConnect SDK")
           {:fx [[:effects.wallet-connect/init
                  {:on-success #(rf/dispatch [:wallet-connect/on-init-success %])
                   :on-fail    #(rf/dispatch [:wallet-connect/on-init-fail %])}]]})
       ;; NOTE: when offline, fetching persistent sessions without initializing WC
       {:fx [[:dispatch [:wallet-connect/get-sessions]]]}))))

(rf/reg-event-fx
 :wallet-connect/on-init-success
 (fn [{:keys [db]} [web3-wallet]]
   (log/info "WalletConnect SDK initialisation successful")
   {:db (assoc db :wallet-connect/web3-wallet web3-wallet)
    :fx [[:dispatch [:wallet-connect/register-event-listeners]]
         [:dispatch [:wallet-connect/get-sessions]]
         [:dispatch [:wallet-connect/pair-with-pending-deeplink]]]}))

(rf/reg-event-fx
 :wallet-connect/reload-on-network-change
 (fn [{:keys [db]} [is-connected?]]
   (let [logged-in? (-> db :profile/profile boolean)]
     (when (and is-connected? logged-in?)
       (log/info "Re-Initialising WalletConnect SDK due to network change")
       {:fx [[:dispatch [:wallet-connect/init]]]}))))

(defn- on-session-proposal
  [data]
  (rf/dispatch [:wallet-connect/on-session-proposal data]))

(defn- on-session-request
  [data]
  (rf/dispatch [:wallet-connect/on-session-request data]))

(defn- on-session-delete
  [data]
  (rf/dispatch [:wallet-connect/on-session-delete data]))


(rf/reg-event-fx
 :wallet-connect/register-event-listeners
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-proposal-event
             on-session-proposal]]
           [:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-request-event
             on-session-request]]
           [:effects.wallet-connect/register-event-listener
            [web3-wallet
             constants/wallet-connect-session-delete-event
             on-session-delete]]]})))

(rf/reg-event-fx
 :wallet-connect/unregister-event-listeners
 (fn [{:keys [db]}]
   (let [web3-wallet (get db :wallet-connect/web3-wallet)]
     {:fx [[:effects.wallet-connect/unregister-event-listener
            [web3-wallet
             constants/wallet-connect-session-proposal-event
             on-session-proposal]]
           [:effects.wallet-connect/unregister-event-listener
            [web3-wallet
             constants/wallet-connect-session-request-event
             on-session-request]]
           [:effects.wallet-connect/unregister-event-listener
            [web3-wallet
             constants/wallet-connect-session-delete-event
             on-session-delete]]]})))

(rf/reg-event-fx
 :wallet-connect/on-init-fail
 (fn [_ [error]]
   (log/error "Failed to initialize Wallet Connect"
              {:error error
               :event :wallet-connect/on-init-fail})))

(rf/reg-event-fx
 :wallet-connect/on-session-request
 (fn [{:keys [db]} [event]]
   (if (networks/event-should-be-handled? db event)
     {:fx [[:dispatch [:wallet-connect/process-session-request event]]]}
     {:fx [[:dispatch
            [:wallet-connect/show-session-networks-unsupported-toast event]]
           [:dispatch
            [:wallet-connect/send-response
             {:request event
              :error   (wallet-connect/get-sdk-error
                        constants/wallet-connect-user-rejected-chains-error-key)}]]]})))

(rf/reg-event-fx
 :wallet-connect/reset-current-request
 (fn [{:keys [db]}]
   {:db (dissoc db :wallet-connect/current-request)}))

(rf/reg-event-fx
 :wallet-connect/no-internet-toast
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:toasts/upsert
           {:type  :negative
            :theme (:theme db)
            :text  (i18n/label :t/wallet-connect-no-internet-warning)}]]]}))
