(ns status-im.common.standard-authentication.events
  (:require
    [utils.re-frame :as rf]))

(rf/reg-event-fx :standard-auth/on-biometric-success
 (fn [{:keys [db]} [callback]]
   (let [key-uid (get-in db [:profile/profile :key-uid])]
     {:fx [[:keychain/get-user-password [key-uid callback]]]})))
