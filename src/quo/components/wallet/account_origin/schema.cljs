(ns quo.components.wallet.account-origin.schema)

(def ^:private ?base
  [:map
   [:type {:optional true} [:enum :default-keypair :recovery-phrase :private-key]]
   [:stored {:optional true} [:enum :on-device :on-keycard]]])

(def ^:private ?keypair-name
  [:map
   [:keypair-name {:optional true} [:maybe :string]]])

(def ^:private ?default-keypair
  [:schema.common/map {:optional true :maybe true}
   [:profile-picture :schema.common/image-source]
   [:derivation-path :string]
   [:on-press fn?]
   [:customization-color :schema.common/customization-color]])

(def ^:private ?recovery-phrase
  [:map
   [:derivation-path {:optional true} [:maybe :string]]
   [:on-press {:optional true} [:maybe fn?]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:multi {:dispatch :type}
      [:default-keypair [:merge ?base ?default-keypair ?keypair-name]]
      [:recovery-phrase [:merge ?base ?recovery-phrase ?keypair-name]]
      [:private-key ?base]]]]
   :any])
