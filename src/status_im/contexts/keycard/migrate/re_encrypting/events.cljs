(ns status-im.contexts.keycard.migrate.re-encrypting.events
  (:require [clojure.string :as string]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :keycard/migration.convert-to-keycard-profile
 (fn [{:keys [db]} [{:keys [key-uid instance-uid encryption-public-key]}]]
   (let [{:keys [masked-password]}   (get-in db [:keycard :migration])
         {:keys [pairing paired-on]} (get-in db [:keycard :pairings instance-uid])
         {:keys [kdfIterations]}     (:profile/profile db)]
     {:fx [[:dispatch [:keycard/disconnect]]
           [:dispatch [:navigate-back]]
           [:dispatch [:open-modal :screen/keycard.re-encrypting]]
           [:effects.profile/convert-to-keycard-profile
            {:profile
             {:key-uid         key-uid
              :keycard-pairing pairing
              :kdfIterations   kdfIterations}
             :settings
             {:keycard-instance-uid instance-uid
              :keycard-paired-on    paired-on
              :keycard-pairing      pairing}
             :password
             (security/safe-unmask-data masked-password)
             :new-password
             encryption-public-key
             :callback
             (fn [{:keys [error]}]
               (if (string/blank? error)
                 (rf/dispatch [:navigate-to :screen/keycard.migrate.success])
                 (rf/dispatch [:navigate-to :screen/keycard.migrate.fail])))}]]})))
