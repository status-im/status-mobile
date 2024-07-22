(ns quo.components.list-items.missing-keypair.schema)

(def ^:private ?base
  [:map
   [:blur? {:optional true} [:maybe :boolean]]
   [:keypair
    [:map
     [:key-uid :string]
     [:name :string]
     [:accounts
      [:sequential
       [:map {:closed true}
        [:type [:enum :default]]
        [:emoji :string]
        [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]]]])

(def ^:private ?on-option-press
  [:map
   [:on-options-press {:optional true} [:maybe fn?]]])

(def ?schema
  [:=>
   [:cat
    [:merge ?base ?on-option-press]]
   :any])
