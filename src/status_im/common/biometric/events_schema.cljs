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
   :schema.re-frame/event-fx])

(def ?show-message
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple keyword?]]]
   :schema.re-frame/event-fx])

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
   :schema.re-frame/event-fx])

(def ?enable-biometrics
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:fn {:error/message "Should be an instance of security/MaskedData"}
       (fn [pw] (instance? security/MaskedData pw))]]]]
   :schema.re-frame/event-fx])

(def ?disable-biometrics
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]]
   :schema.re-frame/event-fx])

(def ?check-if-biometrics-available
  [:=>
   [:cat
    [:map {:closed true}
     [:key-uid string?]
     [:on-success {:optional true} [:maybe fn?]]
     [:on-fail {:optional true} [:maybe fn?]]]]
   :any])
