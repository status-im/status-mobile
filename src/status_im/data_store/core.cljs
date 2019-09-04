(ns status-im.data-store.core
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as realm]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.native-module.core :as status]
            [status-im.protocol.core :as protocol]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(fx/defn multiaccount-db-removed
  {:events [::multiaccount-db-removed]}
  [{:keys [db]}]
  {:db (assoc-in db [:multiaccounts/login :processing] false)})

(defn change-multiaccount
  [address password encryption-key create-database-if-not-exist?]
  (log/debug "changing multiaccount to: " address)
  (..
   (js/Promise.
    (fn [on-success on-error]
      (try
        (realm/close-account-realm)
        (on-success)
        (catch :default e
          (on-error {:message (str e)
                     :error   :closing-account-failed})))))
   (then
    (if create-database-if-not-exist?
      #(js/Promise. (fn [on-success] (on-success)))
      #(realm/db-exists? address)))
   (then
    #(realm/check-db-encryption address password encryption-key))
   (then
    #(realm/open-account address password encryption-key))))

(defn merge-events-of-type [success-events event-type]
  ;; merges data value of events of specified type together
  ;; keeps the other events intact
  ;; [[:e1 [:d1]] [:e1 [:d2]]] => [[:e1 [:d1 :d2]]]
  (let [event-to-merge? (fn [event]
                          (and (vector? event)
                               (= (first event) event-type)
                               (vector? (second event))))
        unmergeable-events (filter (complement event-to-merge?) success-events)
        mergeable-events (filter event-to-merge? success-events)]
    (into []
          (into unmergeable-events
                (when-not (empty? mergeable-events)
                  (let [merged-values (reduce into
                                              (map second mergeable-events))]
                    [(into [event-type]
                           (when merged-values
                             [merged-values]))]))))))

(defn- merge-persistence-events [success-events]
  (merge-events-of-type success-events :message/messages-persisted))

(defn- perform-transactions [raw-transactions realm]
  (let [success-events (keep :success-event raw-transactions)
        transactions   (map (fn [{:keys [transaction] :as f}]
                              (or transaction f)) raw-transactions)]
    (realm/write realm #(doseq [transaction transactions]
                          (transaction realm)))
    (let [optimized-events (merge-persistence-events success-events)]
      (doseq [event optimized-events]
        (re-frame/dispatch event)))))

(re-frame/reg-fx
 :data-store/tx
 (fn [transactions]
   (async/go (async/>! realm/realm-queue
                       (partial perform-transactions
                                transactions
                                @realm/account-realm)))))

(re-frame/reg-fx
 ::init-store
 (fn []
   (realm/ensure-directories)))

(handlers/register-handler-fx
 ::multiaccount-data-reset-accepted
 (fn [_ [_ address]]
   {::reset-multiaccount-data address}))

(re-frame/reg-fx
 ::reset-multiaccount-data
 (fn [address]
   (let [callback #(re-frame/dispatch [::multiaccount-db-removed])]
     (.. (realm/delete-multiaccount-realm address)
         (then callback)
         (catch callback)))))

(fx/defn show-migration-error-dialog
  [{:keys [db]} realm-error]
  (let [{:keys [message]} realm-error
        address           (get-in db [:multiaccounts/login :address])
        erase-button (i18n/label :migrations-erase-multiaccounts-data-button)]
    {:ui/show-confirmation
     {:title               (i18n/label :invalid-key-title)
      :content             (i18n/label
                            :invalid-key-content
                            {:message                         message
                             :erase-multiaccounts-data-button-text erase-button})
      :confirm-button-text (i18n/label :invalid-key-confirm)
      :on-accept           #(re-frame/dispatch
                             [::multiaccount-data-reset-accepted address])}}))

(fx/defn verify-callback
  {:events [::verify-success]}
  [{:keys [db] :as cofx} verify-result realm-error]
  (let [data    (types/json->clj verify-result)
        error   (:error data)
        success (empty? error)]
    (if success
      (case (:error realm-error)
        :decryption-failed
        (show-migration-error-dialog cofx realm-error)

        :database-does-not-exist
        (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
          {::change-multiaccount [address password]}))
      {:db (update db :multiaccounts/login assoc
                   :error error
                   :processing false)})))

(fx/defn migrations-failed
  [{:keys [db]} {:keys [realm-error erase-button]}]
  (let [{:keys [message details]} realm-error
        address (get-in db [:multiaccounts/login :address])]
    {:ui/show-confirmation
     {:title               (i18n/label :migrations-failed-title)
      :content             (i18n/label
                            :migrations-failed-content
                            (merge
                             {:message                         message
                              :erase-multiaccounts-data-button-text erase-button}
                             details))
      :confirm-button-text erase-button
      :on-accept           #(re-frame/dispatch
                             [::multiaccount-data-reset-accepted address])}}))

(fx/defn verify-multiaccount
  [{:keys [db] :as cofx} {:keys [realm-error]}]
  (let [{:keys [address password]} (multiaccounts.model/credentials cofx)]
    {:db (assoc db :realm-error realm-error)
     ::verify [address password (:realm-error db)]}))

(fx/defn unknown-realm-error
  [cofx {:keys [realm-error erase-button]}]
  (let [{:keys [message]} realm-error
        {:keys [address]} (multiaccounts.model/credentials cofx)]
    {:ui/show-confirmation
     {:title               (i18n/label :unknown-realm-error)
      :content             (i18n/label
                            :unknown-realm-error-content
                            {:message                         message
                             :erase-multiaccounts-data-button-text erase-button})
      :confirm-button-text (i18n/label :invalid-key-confirm)
      :on-accept           #(re-frame/dispatch
                             [::multiaccount-data-reset-accepted address])}}))

(fx/defn handle-change-multiaccount-error
  {:events [::multiaccount-change-error]}
  [{:keys [db] :as cofx} error]
  (let [{:keys [error] :as realm-error}
        (if (map? error)
          error
          {:message (str error)})
        erase-button (i18n/label :migrations-erase-multiaccounts-data-button)]
    (fx/merge
     cofx
     {:db (assoc-in db [:multiaccounts/login :save-password?] false)}
     (case error
       :migrations-failed
       (migrations-failed {:realm-error  realm-error
                           :erase-button erase-button})

       (:database-does-not-exist :decryption-failed)
       (verify-multiaccount {:realm-error realm-error})

       (unknown-realm-error {:realm-error  realm-error
                             :erase-button erase-button})))))

(re-frame/reg-fx
 ::verify
 (fn [[address password realm-error]]
   (status/verify address
                  (ethereum/sha3 (security/safe-unmask-data password))
                  #(re-frame/dispatch [::verify-success % realm-error]))))

(fx/defn handle-change-multiaccount-success
  {:events [::multiaccount-change-success]
   :interceptors [(re-frame/inject-cofx :data-store/all-chat-requests-ranges)]}
  [{:data-store/keys [mailserver-ranges] :as cofx}]
  (protocol/initialize-protocol cofx {:mailserver-ranges mailserver-ranges}))

(defn change-multiaccount!
  [address password create-database-if-not-exist?]
  ;; No matter what is the keychain we use, as checks are done on decrypting base
  (.. (keychain/safe-get-encryption-key)
      (then #(change-multiaccount address password % create-database-if-not-exist?))
      (then #(re-frame/dispatch [::multiaccount-change-success address]))
      (catch (fn [error]
               (log/warn "Could not change multiaccount" error)
               ;; If all else fails we fallback to showing initial error
               (re-frame/dispatch [::multiaccount-change-error error])))))

(re-frame/reg-fx
 ::change-multiaccount
 (fn [[address password]]
   (change-multiaccount! address (security/safe-unmask-data password) false)))

(re-frame/reg-fx
 ::create-multiaccount
 (fn [[address password]]
   (change-multiaccount! address (security/safe-unmask-data password) true)))
