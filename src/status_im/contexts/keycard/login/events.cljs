(ns status-im.contexts.keycard.login.events
  (:require [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard.login/on-get-keys-success
 (fn [{:keys [db]} [data]]
   (let [{:keys [key-uid encryption-public-key
                 whisper-private-key]} data
         profile                       (get-in db [:profile/profiles-overview key-uid])]
     {:db
      (-> db
          (dissoc :keycard)
          (update :profile/login assoc
                  :password      encryption-public-key
                  :key-uid       key-uid
                  :name          (:name profile)))
      :fx [[:dispatch [:keycard/disconnect]]
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
        :fx [[:dispatch [:keycard/disconnect]]
             [:effects.keycard/login-with-keycard
              {:password            encryption-public-key
               :whisper-private-key whisper-private-key
               :key-uid             key-uid}]]}))))
