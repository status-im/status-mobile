(ns status-im.common.biometric.events-schema
  (:require
    [status-im.constants :as constants]
    [utils.security.core :as security]))

(def ?set-supported-type
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:enum
       constants/biometrics-type-android
       constants/biometrics-type-face-id
       constants/biometrics-type-touch-id]]]]
   :any])

(def ?show-message
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple keyword?]]]
   :any])

(def ?authenticate
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:map {:closed true}
       [:on-success {:optional true} fn?]
       [:on-fail {:optional true} fn?]
       [:on-cancel {:optional true} fn?]
       [:prompt-message {:optional true} :string]]]]]
   :any])

(def ?enable-biometrics
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:fn {:error/message "Should be an instance of security/MaskedData"}
       (fn [pw] (instance? security/MaskedData pw))]]]]
   :any])

(def ?disable-biometrics
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]]
   :any])
