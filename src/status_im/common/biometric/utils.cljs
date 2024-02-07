(ns status-im.common.biometric.utils
  (:require
    [native-module.core :as native-module]
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def android-device-blacklisted?
  (and platform/android? (= (:brand (native-module/get-device-model-info)) "bannedbrand")))

(defn get-label-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-android (i18n/label :t/biometric-fingerprint)
    constants/biometrics-type-face-id (i18n/label :t/biometric-faceid)
    (i18n/label :t/biometric-touchid)))

(defn get-icon-by-type
  [biometric-type]
  (condp = biometric-type
    constants/biometrics-type-face-id :i/face-id
    :i/touch-id))

;; NOTE: move to utils.re-frame?
(defn handle-cb
  "Handles re-frame callbacks that are passed as anonymous fn or as dispatch vectors to effects.

  e.g.:\n
  * with dispatch vector - `:on-success [:my-ns/my-event]`\n
  * with fn - `:on-success #(rf/dispatch [:my-ns/my-event])`"
  [cb & args]
  (when cb
    (cond
      (fn? cb)     (apply cb args)
      (vector? cb) (rf/dispatch (into cb args)))))
