(ns status-im.contexts.keycard.events
  (:require [re-frame.core :as rf]
            status-im.contexts.keycard.create.events
            status-im.contexts.keycard.login.events
            status-im.contexts.keycard.migrate.events
            status-im.contexts.keycard.migrate.re-encrypting.events
            status-im.contexts.keycard.nfc.events
            status-im.contexts.keycard.nfc.sheets.events
            status-im.contexts.keycard.pin.events
            status-im.contexts.keycard.sign.events
            [status-im.contexts.keycard.utils :as keycard.utils]
            [utils.address :as address]
            utils.datetime
            [utils.ethereum.eip.eip55 :as eip55]))

(rf/reg-event-fx :keycard/on-card-connected
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:keycard :card-connected?] true)
    :fx [(when-let [event (get-in db [:keycard :on-card-connected-event-vector])]
           [:dispatch event])]}))

(rf/reg-event-fx :keycard/on-card-disconnected
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:keycard :card-connected?] false)
    :fx [(when-let [event (get-in db [:keycard :on-card-disconnected-event-vector])]
           [:dispatch event])]}))

(rf/reg-event-fx :keycard/on-card-new-pairing
 (fn [{:keys [db]} [{:keys [pairing instanceUID]}]]
   (when (and instanceUID pairing)
     (let [pairings     (get-in db [:keycard :pairings])
           new-pairings (assoc pairings
                               instanceUID
                               {:pairing   pairing
                                :paired-on (utils.datetime/timestamp)})]
       {:db                       (assoc-in db [:keycard :pairings] new-pairings)
        :keycard/persist-pairings new-pairings}))))

(rf/reg-event-fx :keycard/on-retrieve-pairings-success
 (fn [{:keys [db]} [pairings]]
   {:db (assoc-in db [:keycard :pairings] pairings)
    :fx [[:effects.keycard/set-pairing-to-keycard pairings]]}))

(rf/reg-event-fx :keycard/on-action-with-pin-error
 (fn [{:keys [db]} [error]]
   (let [tag-was-lost?     (keycard.utils/tag-lost? (:error error))
         pin-retries-count (keycard.utils/pin-retries (:error error))]
     (when-not (or tag-was-lost? (nil? pin-retries-count))
       {:db (-> db
                (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries-count)
                (assoc-in [:keycard :pin :text] "")
                (assoc-in [:keycard :pin :status] :error))
        :fx [[:dispatch [:keycard/disconnect]]
             (when (zero? pin-retries-count)
               [:dispatch
                [:keycard/on-application-info-error
                 :keycard/error.keycard-locked]])]}))))

(rf/reg-event-fx :keycard/get-keys
 (fn [_ [data]]
   {:effects.keycard/get-keys data}))

(rf/reg-event-fx :keycard/get-more-keys
 (fn [_ [data]]
   {:effects.keycard/get-more-keys data}))

(rf/reg-event-fx :keycard/export-key
 (fn [_ [data]]
   {:effects.keycard/export-key data}))

(rf/reg-event-fx :keycard/connect-derive-address-and-add-account
 (fn [_ [{:keys [pin derivation-path key-uid account-preferences]}]]
   {:fx [[:dispatch
          [:keycard/connect
           {:key-uid key-uid
            :on-success
            (fn []
              (rf/dispatch
               [:keycard/export-key
                {:pin        pin
                 :path       derivation-path
                 :on-success (fn [public-key]
                               (let [derived-account {:public-key (str "0x" public-key)
                                                      :address    (eip55/address->checksum
                                                                   (str "0x"
                                                                        (address/public-key->address
                                                                         (subs public-key 2))))
                                                      :path       derivation-path}]
                                 (rf/dispatch [:keycard/disconnect])
                                 (rf/dispatch [:wallet/add-account
                                               (assoc account-preferences :key-uid key-uid)
                                               derived-account])))
                 :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]))}]]]}))

(rf/reg-event-fx :keycard/cancel-connection
 (fn [{:keys [db]}]
   {:db (update db
                :keycard
                dissoc
                :card-connected?
                :on-card-connected-event-vector
                :on-nfc-cancelled-event-vector)}))

(rf/reg-event-fx :keycard/disconnect
 (fn [_ _]
   {:fx [[:dispatch [:keycard/cancel-connection]]
         [:dispatch [:keycard/hide-connection-sheet]]]}))

(rf/reg-event-fx :keycard/on-application-info-error
 (fn [{:keys [db]} [error]]
   {:db (assoc-in db [:keycard :application-info-error] error)
    :fx [[:dispatch [:keycard/disconnect]]
         [:dispatch
          [:open-modal
           (if (= :keycard/error.not-keycard error)
             :screen/keycard.not-keycard
             :screen/keycard.error)]]]}))

(rf/reg-event-fx :keycard/update-application-info
 (fn [{:keys [db]} [app-info]]
   {:db (update db
                :keycard
                #(-> %
                     (assoc :application-info app-info)
                     (dissoc :application-info-error)))}))

(rf/reg-event-fx :keycard/get-application-info
 (fn [_ [{:keys [key-uid on-success on-error]}]]
   {:effects.keycard/get-application-info
    {:on-success (fn [app-info]
                   (rf/dispatch [:keycard/update-application-info app-info])
                   (if-let [error (keycard.utils/validate-application-info key-uid app-info)]
                     (if on-error
                       (on-error error)
                       (rf/dispatch [:keycard/on-application-info-error error]))
                     (when on-success (on-success app-info))))
     :on-failure (fn [error]
                   (when-not (keycard.utils/tag-lost? (:error error))
                     (if on-error
                       (on-error :keycard/error.not-keycard)
                       (rf/dispatch [:keycard/on-application-info-error
                                     :keycard/error.not-keycard]))))}}))

(rf/reg-event-fx :keycard/connect
 (fn [{:keys [db]} [{:keys [key-uid on-success on-error on-connect-event-vector]}]]
   (let [event-vector
         (or on-connect-event-vector
             [:keycard/get-application-info
              {:key-uid    key-uid
               :on-success on-success
               :on-error   on-error}])]
     {:db (assoc-in db [:keycard :on-card-connected-event-vector] event-vector)
      :fx [[:dispatch
            [:keycard/show-connection-sheet
             {:on-cancel-event-vector [:keycard/cancel-connection]}]]
           (when (get-in db [:keycard :card-connected?])
             [:dispatch event-vector])]})))
