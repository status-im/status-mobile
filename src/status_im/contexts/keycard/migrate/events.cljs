(ns status-im.contexts.keycard.migrate.events
  (:require [clojure.string :as string]
            [status-im.contexts.keycard.pin.view :as keycard.pin]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/migration.check-empty-card
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:key-uid (get-in db [:profile/profile :key-uid])
            :on-success
            (fn []
              ;;TODO keys already on the keycard, new flow needs to be implemented
              ;; https://github.com/status-im/status-mobile/issues/21446
              (rf/dispatch [:keycard/disconnect])
              (rf/dispatch [:open-modal :screen/keycard.authorise
                            {:on-success
                             #(rf/dispatch [:keycard/migration.authorisation-success %])}]))
            :on-error
            (fn [error]
              (if (= error :keycard/error.keycard-blank)
                (do
                  (rf/dispatch [:keycard/disconnect])
                  (rf/dispatch [:open-modal :screen/keycard.empty]))
                (rf/dispatch [:keycard/on-application-info-error error])))}]]]}))

(defn get-application-info-and-continue
  [key-uid]
  (rf/dispatch [:keycard/get-application-info
                {:key-uid key-uid
                 :on-success #(rf/dispatch [:keycard/migration.continue])
                 :on-error
                 (fn [error]
                   (if (= error :keycard/error.keycard-blank)
                     (rf/dispatch [:keycard/migration.continue])
                     (rf/dispatch [:keycard/on-application-info-error error])))}]))

(rf/reg-event-fx :keycard/migration.continue
 (fn [{:keys [db]}]
   (let [key-uid                                (get-in db [:profile/profile :key-uid])
         {:keys [initialized? has-master-key?]} (get-in db [:keycard :application-info])
         {:keys [masked-phrase pin]}            (get-in db [:keycard :migration])
         on-failure                             (fn []
                                                  (rf/dispatch [:keycard/disconnect])
                                                  (rf/dispatch [:navigate-to
                                                                :screen/keycard.migrate.fail]))]
     (cond

       (not initialized?)
       {:fx [[:keycard/init-card
              {:pin        pin
               :on-success #(get-application-info-and-continue key-uid)
               :on-failure on-failure}]]}

       (not has-master-key?)
       {:fx [[:effects.keycard/generate-and-load-key
              {:mnemonic   (security/safe-unmask-data masked-phrase)
               :pin        pin
               :on-success #(get-application-info-and-continue key-uid)
               :on-failure on-failure}]]}

       :else
       {:fx [[:effects.keycard/get-keys
              {:pin        pin
               :on-success #(rf/dispatch [:keycard/migration.convert-to-keycard-profile %])
               :on-failure on-failure}]]}))))

(rf/reg-event-fx :keycard/migration.start
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:key-uid (get-in db [:profile/profile :key-uid])
            :on-success #(rf/dispatch [:keycard/migration.continue])
            :on-error
            (fn [error]
              (if (= error :keycard/error.keycard-blank)
                (rf/dispatch [:keycard/migration.continue])
                (rf/dispatch [:keycard/on-application-info-error error])))}]]]}))

(rf/reg-event-fx :keycard/migration.get-phrase
 (fn [{:keys [db]}]
   {:db (assoc-in db [:keycard :migration] nil)
    :fx [[:dispatch [:navigate-back]]
         (if (string/blank? (get-in db [:profile/profile :mnemonic]))
           [:dispatch
            [:open-modal :screen/use-recovery-phrase
             {:on-success #(rf/dispatch [:keycard/migration.phrase-entered %])}]]
           [:dispatch
            [:open-modal :screen/backup-recovery-phrase
             {:on-success #(rf/dispatch [:keycard/migration.phrase-backed-up %])}]])]}))

(rf/reg-event-fx :keycard/migration.phrase-entered
 (fn [{:keys [db]} [{:keys [phrase]}]]
   {:db (assoc-in db [:keycard :migration :masked-phrase] (security/mask-data phrase))
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.authorise
           {:on-success #(rf/dispatch [:keycard/migration.authorisation-success %])}]]]}))

(rf/reg-event-fx :keycard/migration.phrase-backed-up
 (fn [{:keys [db]}]
   {:db (assoc-in db
         [:keycard :migration :masked-phrase]
         (security/mask-data (get-in db [:profile/profile :mnemonic])))
    :fx [[:dispatch [:profile.settings/profile-update :mnemonic nil]]
         [:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.authorise
           {:on-success #(rf/dispatch [:keycard/migration.authorisation-success %])}]]]}))

(rf/reg-event-fx :keycard/migration.authorisation-success
 (fn [{:keys [db]} [masked-password]]
   (let [{:keys [initialized?]} (get-in db [:keycard :application-info])]
     {:db (assoc-in db [:keycard :migration :masked-password] masked-password)
      :fx [[:dispatch [:navigate-back]]
           (if initialized?
             [:dispatch
              [:show-bottom-sheet
               {:content (fn []
                           [keycard.pin/auth
                            {:on-complete #(rf/dispatch [:keycard/migration.pin-created %])}])}]]
             [:dispatch
              [:open-modal :screen/keycard.pin.create
               {:on-complete (fn [new-pin]
                               (rf/dispatch [:navigate-back])
                               (rf/dispatch [:keycard/migration.pin-created new-pin]))}]])]})))

(rf/reg-event-fx :keycard/migration.pin-created
 (fn [{:keys [db]} [pin]]
   {:db (assoc-in db [:keycard :migration :pin] pin)
    :fx [[:dispatch [:open-modal :screen/keycard.migrate]]]}))
