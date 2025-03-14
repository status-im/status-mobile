(ns status-im.contexts.keycard.backup.events
  (:require [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/backup.generate-and-load-key
 (fn [{:keys [db]}]
   (let [{:keys [masked-phrase masked-pin]} (get-in db [:keycard :backup])]
     {:fx [[:effects.keycard/generate-and-load-key
            {:mnemonic   (security/safe-unmask-data masked-phrase)
             :pin        (security/safe-unmask-data masked-pin)
             :on-success (fn []
                           (rf/dispatch [:keycard/disconnect])
                           (rf/dispatch [:navigate-back])
                           (rf/dispatch [:open-modal :screen/keycard.backup.success]))
             :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]})))

;; STEP 2: connect and load keys
(rf/reg-event-fx :keycard/backup.ready-to-add-connect
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme        :dark
            :instance-uid (get-in db [:keycard :backup :instance-uid])
            :on-success   #(rf/dispatch [:keycard/backup.generate-and-load-key])}]]]}))

(rf/reg-event-fx :keycard/backup.phrase-entered
 (fn [{:keys [db]} [{:keys [phrase]}]]
   {:db (assoc-in db [:keycard :backup :masked-phrase] phrase)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.backup.ready-to-add
           {:on-press #(rf/dispatch [:keycard/backup.ready-to-add-connect])}]]]}))

(rf/reg-event-fx :keycard/backup.save-instance-uid-and-pin
 (fn [{:keys [db]} [masked-pin]]
   {:db (-> db
            (assoc-in [:keycard :backup :masked-pin] masked-pin)
            (assoc-in [:keycard :backup :instance-uid]
                      (get-in db [:keycard :application-info :instance-uid])))}))

(defn- save-pin-and-navigate-to-phrase
  [pin]
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:keycard/backup.save-instance-uid-and-pin (security/mask-data pin)])
  (rf/dispatch
   [:open-modal :screen/use-recovery-phrase-dark
    {:on-success #(rf/dispatch [:keycard/backup.phrase-entered %])}]))

(defn- show-not-empty-screen
  []
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:open-modal :screen/keycard.backup.not-empty
                {:on-press #(rf/dispatch [:keycard/backup.scan-empty-card])}]))

(rf/reg-event-fx :keycard/backup-on-empty-card
 (fn [_]
   {:fx [[:dispatch
          [:keycard/init.create-or-enter-pin
           {:on-success save-pin-and-navigate-to-phrase}]]]}))

;; STEP 1: connect empty keycard
(rf/reg-event-fx :keycard/backup.scan-empty-card
 (fn [_]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme :dark
            :on-success
            (fn [{:keys [has-master-key?]}]
              (if has-master-key?
                (show-not-empty-screen)
                (do
                  (rf/dispatch [:keycard/disconnect])
                  (rf/dispatch [:keycard/backup-on-empty-card]))))}]]]}))
