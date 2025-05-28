(ns status-im.contexts.browser.events.rpc
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as rf]
            [status-im.contexts.browser.api :as browser.api]
            [status-im.contexts.browser.components.request-accounts-sheet :as request-accounts-sheet]
            [status-im.contexts.browser.db :as browser.db]
            [status-im.contexts.wallet.networks.db :as networks.db]
            [taoensso.timbre :as log]
            [utils.hex :as hex]))

(rf/reg-event-fx :browser.rpc/get-permissions
 (fn [_]
   {:fx [[:fx.promise
          {:promise    browser.api/get-dapp-permissions
           :on-success [:browser/store-permissions]
           :on-error   #(log/error "Failed to get dapp permissions" {:error %})}]]}))

(rf/reg-event-fx :browser/store-permissions
 (fn [{:keys [db]} [permissions]]
   {:db (assoc db :browser/permissions permissions)}))

(rf/reg-event-fx :browser.rpc/process-rpc
 (fn [{:keys [db]} [tab-id rpc-event]]
   (let [tab            (-> db (browser.db/get-tab tab-id))
         connector-dapp {:url     (:dapp-id tab)
                         :name    (-> tab :metadata :title (or (:dapp-id tab)))
                         :iconUrl (-> tab :metadata :logo-url)
                         :chainId (browser.db/get-dapp-chain-id db (:dapp-id tab))}]
     {:fx [[:fx.promise
            {:promise    #(browser.api/call-connector-rpc rpc-event connector-dapp)
             :on-success [:browser.rpc/send tab-id rpc-event]
             :on-error   [:browser.rpc/send-error tab-id rpc-event]}]]})))

(rf/reg-event-fx :browser.rpc/send
 (fn [{:keys [db]} [tab-id rpc-event message]]
   (let [rpc-id      (:id rpc-event)
         webview-ref (get-in db [:browser/tabs-by-id tab-id :ref])]
     {:fx [[:fx.browser/send-message
            [webview-ref
             {:id      rpc-id
              :jsonrpc "2.0"
              :type    "rpcResponse"
              :result  message}]]
           [:dispatch [:browser.rpc/call-next-in-queue]]]})))

(rf/reg-event-fx :browser.rpc/send-error
 (fn [{:keys [db]} [tab-id rpc-event error]]
   (let [rpc-id      (:id rpc-event)
         webview-ref (get-in db [:browser/tabs-by-id tab-id :ref])]
     {:fx [[:fx.browser/send-message
            [webview-ref
             {:id      rpc-id
              :jsonrpc "2.0"
              :type    "rpcResponse"
              :error   error}]]
           [:dispatch [:browser.rpc/call-next-in-queue]]]})))

(rf/reg-event-fx :browser.rpc/on-event
 (fn [{:keys [db]} [tab-id event]]
   (let [current-queue (get db :browser/rpc-queue [])
         rpc-event     (:data event)
         new-queue     (conj current-queue
                             {:tab-id tab-id
                              :event  rpc-event})]
     {:db (assoc db :browser/rpc-queue new-queue)
      :fx [(when (empty? current-queue)
             [:dispatch [:browser.rpc/process-rpc tab-id rpc-event]])]})))

(rf/reg-event-fx :browser.rpc/call-next-in-queue
 (fn [{:keys [db]}]
   (let [current-queue          (get db :browser/rpc-queue [])
         remaining-queue        (-> current-queue rest vec)
         {:keys [tab-id event]} (first remaining-queue)]
     {:db (assoc db :browser/rpc-queue remaining-queue)
      :fx [(when event
             [:dispatch [:browser.rpc/process-rpc tab-id event]])]})))

(rf/reg-event-fx :browser.rpc/approve-request-accounts
 (fn [{:keys [db]} [request address]]
   (let [response {:requestId (:request-id request)
                   :chainId   (browser.db/get-dapp-chain-id db (:url request))
                   :account   address}]
     {:fx [[:fx.promise
            {:promise  #(browser.api/approve-accounts-request response)
             :on-error #(log/error "Failed to approve"
                                   {:error    %
                                    :response response})}]]})))

(rf/reg-event-fx :browser.rpc/reject-request-accounts
 (fn [_ [request]]
   (let [response {:requestId (:request-id request)}]
     {:fx [[:fx.promise
            {:promise  #(browser.api/reject-accounts-request response)
             :on-error #(log/error "Failed to reject"
                                   {:error    %
                                    :response response})}]]})))

(rf/reg-event-fx :browser.rpc/on-permission-granted-signal
 (fn [{:keys [db]} [connector-dapp]]
   (let [{:keys [url name] :as dapp-permission} (-> connector-dapp
                                                    (assoc :chain-id (-> connector-dapp :chains first))
                                                    (dissoc :chains))]
     (log/info "dApp permission granted")
     {:db (assoc-in db [:browser/permissions url] dapp-permission)
      :fx [[:dispatch
            [:toasts/upsert
             {:type :positive
              :text (str "Connected to " name)}]]]})))

(rf/reg-event-fx :browser.rpc/on-permission-revoked-signal
 (fn [{:keys [db]} [{:keys [url name]}]]
   (log/info "dApp permission revoked")
   {:db (update-in db [:browser/permissions] dissoc url)
    :fx [[:dispatch
          [:toasts/upsert
           {:type :positive
            :text (str "Disconnected from " name)}]]]}))

(rf/reg-event-fx :browser.rpc/show-approval-sheet
 (fn [_ [{:keys [content]}]]
   {:fx [[:dispatch
          [:show-bottom-sheet
           {:hide-handle?              true
            :drag-content?             false
            :hide-on-background-press? false
            :content                   content}]]]}))

(rf/reg-event-fx :browser.rpc/on-request-accounts-signal
 (fn [_ [request]]
   {:fx [[:dispatch
          [:browser.rpc/show-approval-sheet
           {:content (fn []
                       [request-accounts-sheet/view {:request request}])}]]]}))

(rf/reg-event-fx :browser.rpc/on-transaction-signal
 (fn [_ [request]]
   {:fx [[:dispatch
          [:browser.rpc/show-approval-sheet
           {:content (fn []
                       [request-accounts-sheet/view {:request request}])}]]]}))

;; (rf/reg-event-fx :browser.rpc/sign-transaction
;;  (fn [{:keys [db]} [request]]
;;    {:fx [[:dispatch
;;           [:standard-auth/authorize-and-sign
;;            {:sign-payload      (:tx-args request)
;;             :theme             :dark
;;             :blur?             false
;;             :on-sign-success   (fn [signatures]
;;                                  (let [ (-> signatures
;;                                             first
;;                                             :signature
;;                                             hex/prefix-hex)]))
;;             :on-sign-error     identity
;;             :auth-button-label "Sign transaction"}]]]}))

(rf/reg-event-fx :browser.rpc/approve-transaction
 (fn [_ [request tx-hash]]
   (let [response {:requestId (:request-id request)
                   :hash      tx-hash}]
     {:fx [[:fx.promise
            {:promise  #(browser.api/approve-accounts-request response)
             :on-error #(log/error "Failed to approve"
                                   {:error    %
                                    :response response})}]]})))

(rf/reg-event-fx :browser.rpc/reject-transaction
 (fn [_ [request]]
   (let [response {:requestId (:request-id request)}]
     {:fx [[:fx.promise
            {:promise  #(browser.api/reject-accounts-request response)
             :on-error #(log/error "Failed to reject"
                                   {:error    %
                                    :response response})}]]})))

(rf/reg-event-fx :browser.rpc/on-chain-switched-signal
 (fn [{:keys [db]} [{:keys [chain-id url]}]]
   (let [{:keys [title]} (browser.db/get-dapp-by-id db url)
         new-chain-id    (hex/hex-to-number chain-id)
         network-name    (networks.db/get-network-name db new-chain-id)]
     {:db (assoc-in db [:browser/permissions url :chain-id] new-chain-id)
      :fx [[:dispatch
            [:toasts/upsert
             {:type :positive
              :text (str title " switched the network to " network-name)}]]]})))
