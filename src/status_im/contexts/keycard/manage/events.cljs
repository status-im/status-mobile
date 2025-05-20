(ns status-im.contexts.keycard.manage.events
  (:require [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard/manage.check-card
 (fn [{:keys [db]}]
   (let [keycard-profile? (not (nil? (get-in db [:profile/profile :keycard-pairing])))
         profile-key-uid  (get-in db [:profile/profile :key-uid])]
     {:fx [[:dispatch
            [:keycard/connect
             {:theme :dark
              :on-success
              (fn [{:keys [has-master-key? key-uid]}]
                (rf/dispatch [:keycard/disconnect])

                (cond
                  (and (not keycard-profile?) (not has-master-key?))
                  (rf/dispatch [:open-modal :screen/keycard.manage.empty-import])

                  (and keycard-profile? (not has-master-key?))
                  (rf/dispatch [:open-modal :screen/keycard.manage.empty-backup])

                  (and (not keycard-profile?) has-master-key? (= profile-key-uid key-uid))
                  (rf/dispatch [:open-modal :screen/keycard.migrate.profile-keys])

                  (and keycard-profile? has-master-key? (= profile-key-uid key-uid))
                  (rf/dispatch [:open-modal :screen/keycard.manage.profile-keys])

                  :else
                  (rf/dispatch [:open-modal :screen/keycard.manage.not-empty-logout])))}]]]})))
