(ns status-im.contexts.keycard.backup.events
  (:require [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/backup.generate-and-load-key
 (fn [{:keys [db]}]
   (let [{:keys [masked-phrase pin]} (get-in db [:keycard :backup])]
     {:fx [[:effects.keycard/generate-and-load-key
            {:mnemonic   (security/safe-unmask-data masked-phrase)
             :pin        pin
             :on-success (fn []
                           (rf/dispatch [:keycard/disconnect])
                           (rf/dispatch [:navigate-back])
                           (rf/dispatch [:open-modal :screen/keycard.backup.success]))
             :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]]})))

(defn- ready-to-add-not-empty
  [error]
  (rf/dispatch [:navigate-back])
  (if (= error :keycard/error.not-keycard)
    (rf/dispatch [:keycard/on-application-info-error error])
    (do
      (rf/dispatch [:keycard/disconnect])
      (rf/dispatch [:open-modal :screen/keycard.backup.not-empty
                    {:on-press #(rf/dispatch [:keycard/backup.ready-to-add-connect])}]))))

(rf/reg-event-fx :keycard/backup.ready-to-add-connect
 (fn [_]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme :dark
            :on-error
            (fn [error]
              (if (= error :keycard/error.keycard-blank)
                (rf/dispatch [:keycard/backup.generate-and-load-key])
                (ready-to-add-not-empty error)))}]]]}))

(defn- scan-empty-card-not-empty
  [error]
  (rf/dispatch [:navigate-back])
  (if (= error :keycard/error.not-keycard)
    (rf/dispatch [:keycard/on-application-info-error error])
    (do
      (rf/dispatch [:keycard/disconnect])
      (rf/dispatch [:open-modal :screen/keycard.backup.not-empty
                    {:on-press #(rf/dispatch [:keycard/backup.scan-empty-card])}]))))

(rf/reg-event-fx :keycard/backup.scan-empty-card
 (fn [_]
   {:fx [[:dispatch
          [:keycard/connect
           {:theme :dark
            :on-error
            (fn [error]
              (if (= error :keycard/error.keycard-blank)
                (do
                  (rf/dispatch [:keycard/disconnect])
                  (rf/dispatch [:keycard/backup.create-or-enter-pin]))
                (scan-empty-card-not-empty error)))}]]]}))

(rf/reg-event-fx :keycard/backup.save-pin
 (fn [{:keys [db]} [pin]]
   {:db (assoc-in db [:keycard :backup :pin] pin)}))

(defn- save-pin-and-navigate-to-phrase
  [pin]
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:keycard/backup.save-pin pin])
  (rf/dispatch
   [:open-modal :screen/use-recovery-phrase-dark
    {:on-success #(rf/dispatch [:keycard/backup.phrase-entered %])}]))

(defn- verify-entered-pin-and-continue
  [pin]
  (rf/dispatch
   [:keycard/connect
    {:theme :dark
     :on-error
     (fn [error]
       (if (= error :keycard/error.keycard-blank)
         (rf/dispatch
          [:keycard/verify-pin
           {:pin        pin
            :on-success #(save-pin-and-navigate-to-phrase pin)
            :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}])
         (rf/dispatch [:keycard/on-application-info-error error])))}]))

(declare init-card-not-empty)

(defn- init-card-with-pin-and-continue
  [pin]
  (rf/dispatch
   [:keycard/connect
    {:theme :dark
     :on-error
     (fn [error]
       (if (= error :keycard/error.keycard-blank)
         (rf/dispatch
          [:keycard/init-card
           {:pin        pin
            :on-success #(rf/dispatch
                          [:keycard/get-application-info
                           {:on-error
                            (fn [error]
                              (if (= error :keycard/error.keycard-blank)
                                (save-pin-and-navigate-to-phrase pin)
                                (init-card-not-empty pin error)))}])}])
         (init-card-not-empty pin error)))}]))

(defn- init-card-not-empty
  [pin error]
  (rf/dispatch [:navigate-back])
  (if (= error :keycard/error.not-keycard)
    (rf/dispatch [:keycard/on-application-info-error error])
    (do
      (rf/dispatch [:keycard/disconnect])
      (rf/dispatch [:open-modal :screen/keycard.backup.not-empty
                    {:on-press #(init-card-with-pin-and-continue pin)}]))))

(rf/reg-event-fx :keycard/backup.create-or-enter-pin
 (fn [{:keys [db]}]
   (let [{:keys [initialized?]} (get-in db [:keycard :application-info])]
     {:fx [[:dispatch [:navigate-back]]
           (if initialized?
             [:dispatch
              [:open-modal :screen/keycard.pin.enter
               {:on-complete verify-entered-pin-and-continue}]]
             [:dispatch
              [:open-modal :screen/keycard.pin.create
               {:on-complete init-card-with-pin-and-continue}]])]})))

(rf/reg-event-fx :keycard/backup.phrase-entered
 (fn [{:keys [db]} [{:keys [phrase]}]]
   {:db (assoc-in db [:keycard :backup :masked-phrase] phrase)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch [:open-modal :screen/keycard.backup.ready-to-add]]]}))
