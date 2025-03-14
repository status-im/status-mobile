(ns status-im.contexts.keycard.unblock.events
  (:require [utils.re-frame :as rf]
            [utils.security.core :as security]))

(defn show-different-card
  []
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:open-modal :screen/keycard.different-card]))

(defn show-success
  []
  (rf/dispatch [:keycard/disconnect])
  (rf/dispatch [:navigate-back])
  (rf/dispatch [:open-modal :screen/keycard.unblock.success]))

(rf/reg-event-fx :keycard/unblock.pin-created
 (fn [{:keys [db]} [masked-pin]]
   {:db (-> db
            (assoc-in [:keycard :unblock :masked-pin] masked-pin)
            (assoc-in [:keycard :unblock :instance-uid]
                      (get-in db [:keycard :application-info :instance-uid])))
    :fx [[:dispatch [:navigate-back]]
         [:dispatch [:open-modal :screen/keycard.unblock.ready-to-unblock]]]}))

(rf/reg-event-fx :keycard/unblock.phrase-entered
 (fn [{:keys [db]} [{:keys [phrase]}]]
   {:db (assoc-in db [:keycard :unblock :masked-phrase] phrase)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.pin.create
           {:on-complete #(rf/dispatch [:keycard/unblock.pin-created (security/mask-data %)])}]]]}))

(rf/reg-event-fx :keycard/unblock.generate-and-load-key
 (fn [{:keys [db]}]
   (let [{:keys [masked-phrase masked-pin]} (get-in db [:keycard :unblock])]
     {:fx [[:effects.keycard/generate-and-load-key
            {:mnemonic   (security/safe-unmask-data masked-phrase)
             :pin        (security/safe-unmask-data masked-pin)
             :on-success show-success}]]})))

;; third stage, import keys
;; it's tricky to compare instance-uid for reseted card
(defn import-keys
  []
  (rf/dispatch [:keycard/connect
                {:next-stage? true
                 :on-success
                 (fn [{:keys [has-master-key?]}]
                   (if has-master-key?
                     (show-success)
                     (rf/dispatch [:keycard/unblock.generate-and-load-key])))}]))

;; second stage, init card with pin
;; there is no way to compare instance-uid for reseted card
(defn init-with-pin
  [masked-pin]
  (rf/dispatch
   [:keycard/connect
    {:next-stage? true
     :on-success
     (fn [{:keys [initialized? has-master-key?]}]
       (if has-master-key?
         (show-different-card)
         (if initialized?
           (import-keys)
           (rf/dispatch [:keycard/init-card
                         {:pin        (security/safe-unmask-data masked-pin)
                          :on-success import-keys}]))))}]))

;; first stage, reset card
(rf/reg-event-fx :keycard/unblock
 (fn [{:keys [db]}]
   (let [masked-pin (get-in db [:keycard :unblock :masked-pin])]
     {:fx [[:dispatch
            [:keycard/connect
             {:instance-uid (get-in db [:keycard :unblock :instance-uid])
              :on-success
              (fn [{:keys [initialized?]}]
                (if initialized?
                  (show-different-card)
                  (init-with-pin masked-pin)))
              :on-error
              (fn [error]
                (if (or (= error :keycard/error.keycard-frozen)
                        (= error :keycard/error.keycard-locked))
                  (rf/dispatch [:keycard/factory-reset {:on-success #(init-with-pin masked-pin)}])
                  (rf/dispatch [:keycard/on-application-info-error error])))}]]]})))
