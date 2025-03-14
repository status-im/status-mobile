(ns status-im.contexts.keycard.create.events
  (:require [clojure.string :as string]
            [legacy.status-im.ethereum.mnemonic :as mnemonic]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/create.generate-and-load-keys
 (fn [{:keys [db]} [{:keys [has-master-key?]}]]
   (let [{:keys [masked-phrase masked-pin
                 instance-uid]} (get-in db [:keycard :create])
         pin                    (security/safe-unmask-data masked-pin)]
     (if has-master-key?
       {:fx [[:effects.keycard/get-more-keys
              {:pin        pin
               :on-success #(rf/dispatch [:keycard.login/recover-profile-and-login %])
               :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}
       {:fx [[:effects.keycard/generate-and-load-key
              {:mnemonic   (security/safe-unmask-data masked-phrase)
               :pin        pin
               :on-success (fn []
                             (rf/dispatch [:keycard/connect
                                           {:next-stage?  true
                                            :instance-uid instance-uid
                                            :on-success   #(rf/dispatch
                                                            [:keycard/create.generate-and-load-keys
                                                             %])}]))
               :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}))))

;; STEP 3: connect and load keys
(rf/reg-event-fx :keycard/create.connect-and-load-keys
 (fn [{:keys [db]}]
   (let [{:keys [instance-uid]} (get-in db [:keycard :create])]
     {:fx [[:dispatch
            [:keycard/connect
             {:theme :dark
              :instance-uid instance-uid
              :on-success
              #(rf/dispatch [:keycard/create.generate-and-load-keys %])}]]]})))

(rf/reg-event-fx :keycard/create.seed-phrase-entered
 (fn [{:keys [db]} [masked-seed-phrase]]
   {:db (assoc-in db [:keycard :create :masked-phrase] masked-seed-phrase)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.create.ready-to-add
           {:on-continue #(rf/dispatch [:keycard/create.connect-and-load-keys])}]]]}))

(rf/reg-event-fx :keycard/create.phrase-backed-up
 (fn [{:keys [db]} [masked-phrase-vector]]
   {:db (assoc-in db
         [:keycard :create :masked-phrase]
         (->> masked-phrase-vector
              security/safe-unmask-data
              (string/join " ")
              security/mask-data))
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.create.ready-to-add
           {:on-continue #(rf/dispatch [:keycard/create.connect-and-load-keys])}]]]}))

(defn- backup-recovery-phrase-success
  [masked-seed-phrase]
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:open-modal :screen/confirm-backup-dark
                {:masked-seed-phrase masked-seed-phrase
                 :theme              :dark
                 :shell?             true
                 :on-try-again       #(rf/dispatch [:open-modal :screen/backup-recovery-phrase-dark
                                                    {:on-success         backup-recovery-phrase-success
                                                     :masked-seed-phrase (->> masked-seed-phrase
                                                                              security/safe-unmask-data
                                                                              (string/join " ")
                                                                              security/mask-data)
                                                     :revealed?          true}])
                 :on-success         #(rf/dispatch [:keycard/create.phrase-backed-up
                                                    masked-seed-phrase])}]))

(rf/reg-event-fx :keycard/create.generate-phrase
 (fn []
   {:effects.keycard/generate-mnemonic
    {:words      (string/join "\n" mnemonic/dictionary)
     :on-success (fn [phrase]
                   (rf/dispatch [:keycard/disconnect])
                   (rf/dispatch [:navigate-back])
                   (rf/dispatch [:open-modal :screen/backup-recovery-phrase-dark
                                 {:on-success         backup-recovery-phrase-success
                                  :masked-seed-phrase (security/mask-data phrase)}]))}}))

;; STEP 2: connect and generate mnemonic
(rf/reg-event-fx :keycard/create.connect-and-generate-phrase
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme        :dark
            :instance-uid (get-in db [:keycard :create :instance-uid])
            :on-success   #(rf/dispatch [:keycard/create.generate-phrase])}]]]}))

(rf/reg-event-fx :keycard/create.save-instance-uid-and-pin
 (fn [{:keys [db]} [masked-pin]]
   {:db (-> db
            (assoc-in [:keycard :create :masked-pin] masked-pin)
            (assoc-in [:keycard :create :instance-uid]
                      (get-in db [:keycard :application-info :instance-uid])))}))

(defn- save-pin-and-navigate-to-phrase
  [pin create?]
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:keycard/create.save-instance-uid-and-pin (security/mask-data pin)])
  (if create?
    (rf/dispatch
     [:open-modal :screen/keycard.create.ready-to-generate
      {:on-continue #(rf/dispatch [:keycard/create.connect-and-generate-phrase])}])
    (rf/dispatch
     [:open-modal :screen/use-recovery-phrase-dark
      {:on-success (fn [{:keys [phrase]}]
                     (rf/dispatch [:keycard/create.seed-phrase-entered phrase]))}])))

(rf/reg-event-fx :keycard/create.open-empty
 (fn []
   {:fx [[:dispatch
          [:open-modal :screen/keycard.create.empty
           {:on-create
            (fn []
              (rf/dispatch [:navigate-back])
              (rf/dispatch [:keycard/init.create-or-enter-pin
                            {:on-success #(save-pin-and-navigate-to-phrase % true)}]))
            :on-import
            (fn []
              (rf/dispatch [:navigate-back])
              (rf/dispatch [:keycard/init.create-or-enter-pin
                            {:on-success #(save-pin-and-navigate-to-phrase % false)}]))}]]]}))

(defn- on-login
  [db key-uid]
  (rf/dispatch [:navigate-back])
  (if (contains? (:profile/profiles-overview db) key-uid)
    (rf/dispatch [:open-modal :screen/keycard.login.already-added])
    (rf/dispatch [:open-modal :screen/keycard.pin.enter
                  {:on-complete
                   #(rf/dispatch [:keycard.login/prepare-for-profile-recovery %])}])))

;; STEP 1: connect empty keycard
(rf/reg-event-fx :keycard/create.check-empty-card
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme :dark
            :on-success
            (fn [{:keys [has-master-key? key-uid]}]
              (rf/dispatch [:keycard/disconnect])
              (if has-master-key?
                (rf/dispatch [:open-modal :screen/keycard.create.not-empty
                              {:on-login #(on-login db key-uid)}])
                (rf/dispatch [:keycard/create.open-empty])))}]]]}))
