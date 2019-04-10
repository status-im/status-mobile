(ns status-im.extensions.network
  (:require [status-im.utils.handlers :as handlers]
            [status-im.i18n :as i18n]
            [status-im.network.core :as network]))

(handlers/register-handler-fx
 :extensions/network-on-success
 (fn [cofx [_ on-success result]]
   (when on-success (on-success {:value result}))))

(defn- network-id [extension-id chain-id]
  (str extension-id "_" chain-id))

(handlers/register-handler-fx
 :network/select
 (fn [cofx [_ _ {:keys [chain-id on-success on-failure]}]]
   (if-let [network-id (network/get-network-id-for-chain-id cofx chain-id)]
     (network/connect cofx {:network-id network-id
                            :on-success (when on-success #(on-success {:value %}))
                            :on-failure (when on-failure #(on-failure {:value %}))})
     (when on-failure (on-failure {:value (i18n/label :t/extensions-network-not-found)})))))

(handlers/register-handler-fx
 :network/add
 (fn [{:keys [db] :as cofx} [_ {extension-id :id} {:keys [chain-id name url on-success on-failure]}]]
   (network/save cofx {:data {:name {:value name}
                              :url {:value url}
                              :network-id {:value chain-id}
                              :chain {:value :custom}}
                       :network-id  (network-id extension-id chain-id)
                       :success-event (when on-success [:extensions/network-on-success on-success chain-id])
                       :on-failure   (when on-failure #(on-failure {:value %}))
                       :chain-id-unique? false})))

(handlers/register-handler-fx
 :network/remove
 (fn [cofx [_ {extension-id :id} {:keys [chain-id on-success on-failure]}]]
   (let [network-id (network-id extension-id chain-id)]
     (if (network/get-network cofx network-id)
       (network/remove-network cofx network-id (when on-success [:extensions/network-on-success on-success chain-id]))
       (when on-failure (on-failure {:value (i18n/label :t/extensions-chain-id-not-found)}))))))
