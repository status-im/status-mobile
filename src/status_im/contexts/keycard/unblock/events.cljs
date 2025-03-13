(ns status-im.contexts.keycard.unblock.events
  (:require [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/unblock.pin-created
 (fn [{:keys [db]} [pin]]
   {:db (assoc-in db [:keycard :unblock :pin] pin)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch [:open-modal :screen/keycard.unblock.ready-to-unblock]]]}))

(rf/reg-event-fx :keycard/unblock.phrase-entered
 (fn [{:keys [db]} [{:keys [phrase]}]]
   {:db (assoc-in db [:keycard :unblock :masked-phrase] phrase)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.pin.create
           {:on-complete #(rf/dispatch [:keycard/unblock.pin-created %])}]]]}))

(rf/reg-event-fx :keycard/unblock.generate-and-load-key
 (fn [{:keys [db]}]
   (let [{:keys [masked-phrase pin]} (get-in db [:keycard :unblock])]
     {:fx [[:effects.keycard/generate-and-load-key
            {:mnemonic   (security/safe-unmask-data masked-phrase)
             :pin        pin
             :on-success (fn []
                           (rf/dispatch [:keycard/disconnect])
                           (rf/dispatch [:navigate-back])
                           (rf/dispatch [:open-modal :screen/keycard.unblock.success]))}]]})))

;; third stage, import keys
(rf/reg-event-fx :keycard/unblock.import-keys
 (fn []
   {:fx [[:dispatch
          [:keycard/connect.next-stage
           {:on-error (fn [error]
                        (if (= error :keycard/error.keycard-empty)
                          (rf/dispatch [:keycard/unblock.generate-and-load-key])
                          (do
                            (rf/dispatch [:navigate-back])
                            (rf/dispatch [:keycard/on-application-info-error error]))))}]]]}))

;; second stage, check if card initialized
(rf/reg-event-fx :keycard/unblock.init-card-or-import-keys
 (fn [{:keys [db]}]
   (let [pin                    (get-in db [:keycard :unblock :pin])
         {:keys [initialized?]} (get-in db [:keycard :application-info])]
     {:fx [(if initialized?
             [:dispatch [:keycard/unblock.import-keys]]
             [:effects.keycard/init-card
              {:pin        pin
               :on-success #(rf/dispatch [:keycard/unblock.import-keys])}])]})))

;; second stage, init card with pin
(defn init-with-pin
  []
  (rf/dispatch
   [:keycard/connect.next-stage
    {:on-error (fn [error]
                 (if (= error :keycard/error.keycard-empty)
                   (rf/dispatch [:keycard/unblock.init-card-or-import-keys])
                   (do
                     (rf/dispatch [:navigate-back])
                     (rf/dispatch [:keycard/on-application-info-error error]))))}]))

;; first stage, reset card
(rf/reg-event-fx :keycard/unblock
 (fn [{:keys [db]}]
   {:fx [[:dispatch
          [:keycard/connect
           {:key-uid  (get-in db [:keycard :application-info :key-uid])
            :on-error (fn [error]
                        (cond
                          (or (= error :keycard/error.keycard-frozen)
                              (= error :keycard/error.keycard-locked))
                          (rf/dispatch [:keycard/factory-reset {:on-success init-with-pin}])

                          (= error :keycard/error.keycard-empty)
                          (init-with-pin)

                          :else
                          (do
                            (rf/dispatch [:navigate-back])
                            (if (= error :keycard/error.keycard-wrong-profile)
                              (do
                                (rf/dispatch [:keycard/disconnect])
                                (rf/dispatch [:open-modal :screen/keycard.different-card]))
                              (rf/dispatch [:keycard/on-application-info-error error])))))}]]]}))
