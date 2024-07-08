(ns status-im.common.biometric.utils
  (:require
    [native-module.core :as native-module]
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]))

(def android-device-blacklisted?
  (and platform/android? (= (:brand (native-module/get-device-model-info)) "bannedbrand")))

(defn get-label-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-android  (i18n/label :t/biometric-fingerprint)
    constants/biometrics-type-face-id  (i18n/label :t/biometric-faceid)
    constants/biometrics-type-touch-id (i18n/label :t/biometric-touchid)
    (i18n/label :t/biometric)))

(defn get-icon-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-face-id :i/face-id
    :i/touch-id))
