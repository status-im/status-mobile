(ns status-im.contexts.keycard.check.events
  (:require [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard/check-empty-card
 (fn [{:keys [db]}]
   (let [keycard-profile? (not (nil? (get-in db [:profile/profile :keycard-pairing])))]
     {:fx [[:dispatch
            [:keycard/connect
             {:key-uid (get-in db [:profile/profile :key-uid])
              :theme :dark
              :on-success
              (fn []
                (rf/dispatch [:keycard/disconnect])
                (rf/dispatch [:navigate-back])
                (if keycard-profile?
                  (rf/dispatch [:open-modal :screen/keycard.manage.profile-keys])
                  (rf/dispatch [:open-modal :screen/keycard.migrate.profile-keys])))
              :on-error
              (fn [error]
                (rf/dispatch [:navigate-back])
                (if (and (= error :keycard/error.keycard-blank)
                         (not keycard-profile?))
                  (do
                    (rf/dispatch [:keycard/disconnect])
                    (rf/dispatch [:open-modal :screen/keycard.empty]))
                  (rf/dispatch [:keycard/on-application-info-error error])))}]]]})))
