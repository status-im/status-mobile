(ns status-im.subs.standard-authentication
  (:require [status-im.common.biometric.utils :as biometric]
            [status-im.constants :as constants]
            [utils.re-frame :as rf]))

(rf/reg-sub
 :standard-auth/slider-icon
 :<- [:auth-method]
 :<- [:biometrics/supported-type]
 :<- [:keycard/keycard-profile?]
 (fn [[auth-method biometrics-type keycard-pairing?]]
   (let [icons                 {:biometrics (biometric/get-icon-by-type biometrics-type)
                                :password   :password
                                :keycard    :i/keycard}
         auth-with-biometrics? (= auth-method constants/auth-method-biometric)]
     (cond
       keycard-pairing?      (:keycard icons)
       auth-with-biometrics? (:biometrics icons)
       :else                 (:password icons)))))
