(ns status-im.contexts.keycard.login.events
  (:require [status-im.contexts.keycard.utils :as keycard.utils]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard.login/on-get-keys-error
 (fn [{:keys [db]} [error]]
   (log/debug "[keycard] get keys error: " error)
   (let [tag-was-lost?     (keycard.utils/tag-lost? (:error error))
         pin-retries-count (keycard.utils/pin-retries (:error error))]
     (if tag-was-lost?
       {:db (assoc-in db [:keycard :pin :status] nil)}
       (if (nil? pin-retries-count)
         {:effects.utils/show-popup {:title "wrong-keycard"}}
         {:db (-> db
                  (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries-count)
                  (update-in [:keycard :pin] assoc :status :error))
          :fx [[:dispatch [:keycard/hide-connection-sheet]]
               (when (zero? pin-retries-count)
                 [:effects.utils/show-popup {:title "frozen-keycard"}])]})))))

(rf/reg-event-fx :keycard.login/on-get-keys-success
 (fn [{:keys [db]} [data]]
   (let [{:keys [key-uid encryption-public-key
                 whisper-private-key]} data
         key-uid                       (str "0x" key-uid)
         profile                       (get-in db [:profile/profiles-overview key-uid])]
     {:db
      (-> db
          (dissoc :keycard)
          (update :profile/login assoc
                  :password      encryption-public-key
                  :key-uid       key-uid
                  :name          (:name profile)))
      :fx [[:dispatch [:keycard/hide-connection-sheet]]
           [:effects.keycard/login-with-keycard
            {:password            encryption-public-key
             :whisper-private-key whisper-private-key
             :key-uid             key-uid}]]})))

(rf/reg-event-fx :keycard.login/on-get-keys-from-keychain-success
 (fn [{:keys [db]} [key-uid [encryption-public-key whisper-private-key]]]
   (when (and encryption-public-key whisper-private-key)
     (let [profile (get-in db [:profile/profiles-overview key-uid])]
       {:db
        (-> db
            (dissoc :keycard)
            (update :profile/login assoc
                    :password      encryption-public-key
                    :key-uid       key-uid
                    :name          (:name profile)))
        :fx [[:dispatch [:keycard/hide-connection-sheet]]
             [:effects.keycard/login-with-keycard
              {:password            encryption-public-key
               :whisper-private-key whisper-private-key
               :key-uid             key-uid}]]}))))

(rf/reg-event-fx :keycard.login/on-get-application-info-success
 (fn [{:keys [db]} [application-info]]
   (let [profile (get-in db [:profile/profiles-overview (get-in db [:profile/login :key-uid])])
         pin     (get-in db [:keycard :pin :text])
         error   (keycard.utils/validate-application-info profile application-info)]
     (if error
       {:effects.utils/show-popup {:title (str error)}}
       {:db                       (-> db
                                      (assoc-in [:keycard :application-info] application-info)
                                      (assoc-in [:keycard :pin :status] :verifying))
        :effects.keycard/get-keys {:pin        pin
                                   :on-success #(rf/dispatch [:keycard.login/on-get-keys-success %])
                                   :on-failure #(rf/dispatch [:keycard.login/on-get-keys-error %])}}))))

(rf/reg-event-fx :keycard.login/cancel-reading-card
 (fn [{:keys [db]}]
   {:db (assoc-in db [:keycard :on-card-connected-event-vector] nil)}))

(rf/reg-event-fx :keycard/read-card-and-login
 (fn [{:keys [db]}]
   (let [connected?   (get-in db [:keycard :card-connected?])
         event-vector [:keycard/get-application-info
                       {:on-success #(rf/dispatch [:keycard.login/on-get-application-info-success %])}]]
     (log/debug "[keycard] proceed-to-login")
     {:db (assoc-in db [:keycard :on-card-connected-event-vector] event-vector)
      :fx [[:dispatch
            [:keycard/show-connection-sheet
             {:on-cancel-event-vector [:keycard.login/cancel-reading-card]}]]
           (when connected?
             [:dispatch event-vector])]})))
