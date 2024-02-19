(ns status-im.common.standard-authentication.events-schema)

(def ^:private ?authorize-map
  [:map {:closed true}
   [:on-auth-success fn?]
   [:on-auth-fail {:optional true} [:maybe fn?]]
   [:on-close {:optional true} [:maybe fn?]]
   [:auth-button-label {:optional true} [:maybe string?]]
   [:auth-button-icon-left {:optional true} [:maybe keyword?]]
   [:blur? {:optional true} [:maybe boolean?]]
   [:theme {:optional true} [:maybe :schema.common/theme]]])

(def ?authorize
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple ?authorize-map]]]
   :any])

(def ?authorize-with-biometric
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple ?authorize-map]]]
   :any])

(def ?on-biometric-success
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple fn?]]]
   :any])

(def ?on-biometrics-fail
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:maybe fn?]
      [:maybe :schema.common/error]]]]
   :any])

(def ?authorize-with-password
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple ?authorize-map]]]
   :any])
