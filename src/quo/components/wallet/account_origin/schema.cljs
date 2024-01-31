(ns quo.components.wallet.account-origin.schema)

(def ?base
  [:map {:closed true}
   [:type {:optional true} [:enum :default-keypair :recovery-phrase :private-key]]
   [:stored {:optional true} [:enum :on-device :on-keycard]] 
   [:theme :schema.common/theme]])

(def ?default-keypair
  [:map
   [:user-name {:optional true} [:maybe :string]]
   [:profile-picture {:optional true} [:maybe [:or :schema.common/image-source :string]]]
   [:derivation-path {:optional true} [:maybe :string]]
   [:on-press {:optional true} [:maybe fn?]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]])

(def ?recovery-phrase
  [:map
   [:derivation-path {:optional true} [:maybe :string]]
   [:on-press {:optional true} [:maybe fn?]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:default-keypair [:merge ?base ?default-keypair]]
      [:recovery-phrase [:merge ?base ?recovery-phrase]]
      [:private-key ?base]]]]
   :any])