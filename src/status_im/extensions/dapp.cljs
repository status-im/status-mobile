(ns status-im.extensions.dapp
  (:require [status-im.utils.handlers :as handlers]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]))

(handlers/register-handler-fx
 :extensions/dapp-on-success
 (fn [cofx [_ on-success result]]
   (when on-success (on-success {:value result}))))

(handlers/register-handler-fx
 :extensions/dapp-add
 (fn [{db :db :as cofx} [_ _ {:keys [name dapp-url photo-path description on-success on-failure]}]]
   (let [dapps (get-in db [:account/account :dapps])
         exists? (some #(or (= dapp-url (:dapp-url %))
                            (= name (:name %)))
                       (or dapps []))]
     (if exists?
       (when on-failure (on-failure {:value (i18n/label :t/extensions-dapp-already-exists)}))
       (let [data {:name name
                   :dapp-url dapp-url
                   :photo-path photo-path
                   :description description}
             dapps-new (conj dapps data)]
         (accounts.update/account-update cofx
                                         {:dapps dapps-new}
                                         {:success-event (when on-success [:extensions/dapp-on-success on-success name])}))))))

(handlers/register-handler-fx
 :extensions/dapp-remove
 (fn [{db :db :as cofx} [_ _ {:keys [name on-success on-failure]}]]
   (let [dapps (get-in db [:account/account :dapps])
         idx   (some #(when (= name (:name (second %)))
                        (first %))
                     (map-indexed vector dapps))]
     (if (nil? idx)
       (when on-failure (on-failure {:value (i18n/label :t/extensions-dapp-not-found)}))
       (let [dapps-new (vec (concat (subvec dapps 0 idx)
                                    (subvec dapps (inc idx))))]
         (accounts.update/account-update cofx
                                         {:dapps dapps-new}
                                         {:success-event (when on-success [:extensions/dapp-on-success on-success name])}))))))