(ns status-im.contexts.keycard.migrate.events
  (:require [clojure.string :as string]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/migration.generate-and-load-keys
 (fn [{:keys [db]} [{:keys [has-master-key?]}]]
   (let [{:keys [masked-phrase masked-pin
                 instance-uid]} (get-in db [:keycard :migration])
         pin                    (security/safe-unmask-data masked-pin)]
     (if has-master-key?
       {:fx [[:effects.keycard/get-keys
              {:pin        pin
               :on-success #(rf/dispatch [:keycard/migration.convert-to-keycard-profile %])
               :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}
       {:fx [[:effects.keycard/generate-and-load-key
              {:mnemonic   (security/safe-unmask-data masked-phrase)
               :pin        pin
               :on-success (fn []
                             (rf/dispatch
                              [:keycard/connect
                               {:next-stage?  true
                                :instance-uid instance-uid
                                :on-success   #(rf/dispatch
                                                [:keycard/migration.generate-and-load-keys %])}]))
               :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}))))

;; STEP 2: connect and load keys
(rf/reg-event-fx :keycard/migration.connect-and-load-keys
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme        :dark
            :instance-uid (get-in db [:keycard :migration :instance-uid])
            :on-success   #(rf/dispatch [:keycard/migration.generate-and-load-keys %])}]]]}))

(rf/reg-event-fx :keycard/migration.save-instance-uid-and-pin
 (fn [{:keys [db]} [masked-pin]]
   {:db (-> db
            (assoc-in [:keycard :migration :masked-pin] masked-pin)
            (assoc-in [:keycard :migration :instance-uid]
                      (get-in db [:keycard :application-info :instance-uid])))}))

(defn- save-pin-and-show-migrate
  [pin]
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:keycard/migration.save-instance-uid-and-pin (security/mask-data pin)])
  (rf/dispatch [:open-modal :screen/keycard.migrate]))

;; STEP 1: connect and create or enter pin
(rf/reg-event-fx :keycard/migration.authorisation-success
 (fn [{:keys [db]} [masked-password]]
   {:db (assoc-in db [:keycard :migration :masked-password] masked-password)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:keycard/init.create-or-enter-pin
           {:on-success save-pin-and-show-migrate}]]]}))

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

(defn- backup-recovery-phrase-success
  [masked-seed-phrase]
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:open-modal :screen/confirm-backup-dark
                {:masked-seed-phrase masked-seed-phrase
                 :theme :dark
                 :shell? true
                 :on-try-again
                 #(rf/dispatch [:open-modal :screen/backup-recovery-phrase-dark
                                {:on-success         backup-recovery-phrase-success
                                 :masked-seed-phrase (->> masked-seed-phrase
                                                          security/safe-unmask-data
                                                          (string/join " ")
                                                          security/mask-data)
                                 :revealed?          true}])
                 :on-success
                 #(rf/dispatch [:keycard/migration.phrase-backed-up])}]))

(rf/reg-event-fx :keycard/migration.phrase-entered
 (fn [{:keys [db]} [{:keys [phrase]}]]
   {:db (assoc-in db [:keycard :migration :masked-phrase] phrase)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.authorise
           {:on-success #(rf/dispatch [:keycard/migration.authorisation-success %])}]]]}))

(rf/reg-event-fx :keycard/migration.get-phrase
 (fn [{:keys [db]}]
   (let [mnemonic (get-in db [:profile/profile :mnemonic])]
     {:db (assoc-in db [:keycard :migration] nil)
      :fx [[:dispatch [:navigate-back]]
           (if (string/blank? mnemonic)
             [:dispatch
              [:open-modal :screen/use-recovery-phrase-dark
               {:on-success #(rf/dispatch [:keycard/migration.phrase-entered %])}]]
             [:dispatch
              [:open-modal :screen/backup-recovery-phrase-dark
               {:on-success         backup-recovery-phrase-success
                :masked-seed-phrase (security/mask-data mnemonic)}]])]})))
