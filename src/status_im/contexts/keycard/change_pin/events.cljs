(ns status-im.contexts.keycard.change-pin.events
  (:require [status-im.contexts.keycard.utils :as utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/change-pin.enter-current-pin
 (fn [{:keys [db]}]
   {:db (update db :keycard dissoc :change-pin)
    :fx [[:dispatch [:navigate-back]]
         [:dispatch
          [:open-modal :screen/keycard.pin.enter
           {:title       (i18n/label :t/enter-current-keycard-pin)
            :on-complete (fn [current-pin]
                           (rf/dispatch [:navigate-back])
                           (rf/dispatch [:keycard/change-pin.save-current-pin current-pin])
                           (rf/dispatch [:keycard/change-pin.enter-new-pin]))}]]]}))

(rf/reg-event-fx :keycard/change-pin.save-current-pin
 (fn [{:keys [db]} [pin]]
   {:db (assoc-in db [:keycard :change-pin :current-pin] (security/mask-data pin))}))

(rf/reg-event-fx :keycard/change-pin.enter-new-pin
 (fn [_]
   {:fx [[:dispatch
          [:open-modal :screen/keycard.pin.create
           {:title              (i18n/label :t/create-new-keycard-pin)
            :repeat-stage-title (i18n/label :t/repeat-new-keycard-pin)
            :on-complete        (fn [new-pin]
                                  (rf/dispatch [:navigate-back])
                                  (rf/dispatch [:keycard/change-pin.save-new-pin new-pin])
                                  (rf/dispatch [:open-modal :screen/keycard.ready-to-change-pin]))}]]]}))

(rf/reg-event-fx :keycard/change-pin.save-new-pin
 (fn [{:keys [db]} [pin]]
   {:db (assoc-in db [:keycard :change-pin :new-pin] (security/mask-data pin))}))

(defn- change-pin-and-continue
  [current-pin new-pin]
  (rf/dispatch [:keycard/change-pin
                {:on-success  (fn []
                                (rf/dispatch [:navigate-back])
                                (rf/dispatch [:keycard/disconnect])
                                (rf/dispatch [:open-modal :screen/keycard.pin-change-success]))
                 :on-failure  (fn []
                                (rf/dispatch [:navigate-back])
                                (rf/dispatch [:keycard/disconnect])
                                (rf/dispatch [:open-modal :screen/keycard.pin-change-failed]))
                 :current-pin current-pin
                 :new-pin     new-pin}]))

(defn- verify-pin-and-continue
  [current-pin new-pin]
  (rf/dispatch
   [:keycard/verify-pin
    {:pin        current-pin
     :on-success #(change-pin-and-continue current-pin new-pin)
     :on-failure (fn [error]
                   (when-not (utils/tag-lost? (:error error))
                     (rf/dispatch [:keycard/disconnect])
                     (rf/dispatch [:keycard/on-action-with-pin-error error])
                     (rf/dispatch [:keycard/change-pin.enter-current-pin])))}]))

(rf/reg-event-fx :keycard/change-pin.verify-current-pin-and-continue
 (fn [{:keys [db]}]
   (let [{:keys [current-pin new-pin]} (get-in db [:keycard :change-pin])
         unmasked-current-pin          (security/safe-unmask-data current-pin)
         unmasked-new-pin              (security/safe-unmask-data new-pin)]
     {:fx [[:dispatch
            [:keycard/connect
             {:theme      :dark
              :key-uid    (get-in db [:profile/profile :key-uid])
              :on-success #(verify-pin-and-continue unmasked-current-pin unmasked-new-pin)}]]]})))
