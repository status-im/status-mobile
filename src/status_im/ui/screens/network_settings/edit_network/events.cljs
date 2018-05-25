(ns status-im.ui.screens.network-settings.edit-network.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [clojure.string :as string]))

(defn- new-network [{:keys [random-id] :as cofx} network-name upstream-url type]
  (let [data-dir (str "/ethereum/" (name type) "_rpc")
        config   {:NetworkId      (ethereum/chain-keyword->chain-id type)
                  :DataDir        data-dir
                  :UpstreamConfig {:Enabled true
                                   :URL     upstream-url}}]
    {:id         (string/replace random-id "-" "")
     :name       network-name
     :config     config}))

(handlers/register-handler-fx
 :save-new-network
 [(re-frame/inject-cofx :random-id)]
 (fn [{{:networks/keys [manage] :account/keys [account] :as db} :db :as cofx} _]
   (let [{:keys [name url chain]} manage
         network                  (new-network cofx (:value name) (:value url) (:value chain))
         new-networks             (merge {(:id network) network} (:networks account))]
     (handlers-macro/merge-fx cofx
                              {:db       (dissoc db :networks/manage)
                               :dispatch [:navigate-back]}
                              (accounts.utils/account-update {:networks new-networks})))))

(handlers/register-handler-fx
 :network-set-input
 (fn [{db :db} [_ input-key value]]
   {:db (update db :networks/manage merge {input-key {:value value
                                                      :error (and (string? value) (empty? value))}})}))

(handlers/register-handler-fx
 :edit-network
 (fn [{db :db} _]
   {:db       (update-in db [:networks/manage] assoc
                         :name  {:error true}
                         :url   {:error true}
                         :chain {:value :mainnet})
    :dispatch [:navigate-to :edit-network]}))

