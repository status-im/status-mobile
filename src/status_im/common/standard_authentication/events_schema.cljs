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

(def ^:private ?sign-payload
  [:sequential {:min 1}
   [:map {:closed true}
    [:message :string]
    [:address :string]]])

(def ?authorize-and-sign
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:map {:closed true}
       [:sign-payload ?sign-payload]
       [:on-sign-success fn?]
       [:on-sign-error {:optional true} fn?]
       [:on-close {:optional true} [:maybe fn?]]
       [:auth-button-label {:optional true} [:maybe string?]]
       [:auth-button-icon-left {:optional true} [:maybe keyword?]]
       [:blur? {:optional true} [:maybe boolean?]]
       [:theme {:optional true} [:maybe :schema.common/theme]]]]]]
   :any])

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
      [:maybe :schema.common/exception]]]]
   :any])

(def ?authorize-with-password
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:tuple
      [:merge
       ?authorize-map
       [:map {:closed true}
        [:hide-biometrics-button? {:optional true} boolean?]]]]]]
   :any])
