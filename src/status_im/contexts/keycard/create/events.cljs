(ns status-im.contexts.keycard.create.events
  (:require [clojure.string :as string]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/create.check-empty-card
 (fn [_]
   {:fx [[:dispatch
          [:keycard/connect
           {:on-error
            (fn [error]
              (if (= error :keycard/error.keycard-blank)
                (do
                  (rf/dispatch [:keycard/disconnect])
                  (rf/dispatch [:open-modal :screen/keycard.empty-create]))
                (rf/dispatch [:keycard/on-application-info-error error])))}]]]}))

(defn- backup-recovery-phrase-success
  [masked-seed-phrase]
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:open-modal :screen/confirm-backup
                {:masked-seed-phrase masked-seed-phrase
                 :on-success         #(rf/dispatch [:keycard/create.phrase-backed-up %])}]))

(rf/reg-event-fx :keycard/create.get-phrase
 (fn [{:keys [db]}]
   {:db (assoc-in db [:keycard :create] nil)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/backup-recovery-phrase-dark
           {:on-success backup-recovery-phrase-success}]]]}))

(rf/reg-event-fx :keycard/create.phrase-backed-up
 (fn [{:keys [db]} [masked-phrase-vector]]
   {:db (assoc-in db
         [:keycard :create :masked-phrase]
         (->> masked-phrase-vector
              security/safe-unmask-data
              (string/join " ")
              security/mask-data))
    :fx [[:dispatch [:keycard/create.create-or-enter-pin]]]}))

(rf/reg-event-fx :keycard/create.create-or-enter-pin
 (fn [{:keys [db]}]
   (let [{:keys [initialized?]} (get-in db [:keycard :application-info])]
     {:fx [[:dispatch [:navigate-back]]
           (if initialized?
             [:dispatch
              [:open-modal :screen/keycard.pin.enter
               {:on-complete (fn [new-pin]
                               (rf/dispatch [:keycard/create.save-pin new-pin])
                               (rf/dispatch [:keycard/create.start]))}]]
             [:dispatch
              [:open-modal :screen/keycard.pin.create
               {:on-complete (fn [new-pin]
                               (rf/dispatch [:navigate-back])
                               (rf/dispatch [:keycard/create.save-pin new-pin])
                               (rf/dispatch [:open-modal :screen/keycard.create.ready-to-add]))}]])]})))

(rf/reg-event-fx :keycard/create.save-pin
 (fn [{:keys [db]} [pin]]
   {:db (assoc-in db [:keycard :create :pin] pin)}))

(rf/reg-event-fx :keycard/create.start
 (fn [_]
   {:fx [[:dispatch
          [:keycard/connect
           {:on-error
            (fn [error]
              (if (= error :keycard/error.keycard-blank)
                (rf/dispatch [:keycard/create.continue])
                (rf/dispatch [:keycard/on-application-info-error error])))}]]]}))

(defn get-application-info-and-continue
  [init?]
  (rf/dispatch [:keycard/get-application-info
                {:on-success #(rf/dispatch [:keycard/create.continue])
                 :on-error
                 (fn [error]
                   (if (or (= error :keycard/error.keycard-blank)
                           (and (not init?) (= error :keycard/error.keycard-wrong-profile)))
                     (rf/dispatch [:keycard/create.continue])
                     (rf/dispatch [:keycard/on-application-info-error error])))}]))

(rf/reg-event-fx :keycard/create.continue
 (fn [{:keys [db]}]
   (let [{:keys [initialized? has-master-key?]} (get-in db [:keycard :application-info])
         {:keys [masked-phrase pin]}            (get-in db [:keycard :create])]

     (cond

       (not initialized?)
       {:fx [[:keycard/init-card
              {:pin        pin
               :on-success #(get-application-info-and-continue true)}]]}

       (not has-master-key?)
       {:fx [[:effects.keycard/generate-and-load-key
              {:mnemonic   (security/safe-unmask-data masked-phrase)
               :pin        pin
               :on-success #(get-application-info-and-continue false)
               :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}

       :else
       {:fx [[:effects.keycard/get-more-keys
              {:pin        pin
               :on-success #(rf/dispatch [:keycard.login/recover-profile-and-login %])
               :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]}))))
