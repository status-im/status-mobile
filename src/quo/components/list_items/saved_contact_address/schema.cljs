(ns quo.components.list-items.saved-contact-address.schema)

(def ^private ?contact
  [:map
   [:full-name :string]
   [:profile-picture {:optional true} [:maybe [:or :schema.common/image-source :string]]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]])

(def ^private ?account
  [:map
   [:name :string]
   [:address :string]
   [:emoji {:optional true} :string]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]])

(def ^private ?accounts
  [:+ ?account])

(def ^private ?schema
  [:=>
   [:cat
    [:map
     [:contact-props ?contact]
     [:accounts {:optional true} ?accounts]
     [:on-press {:optional true} [:maybe fn?]]
     [:theme :schema.common/theme]
     [:active-state? {:optional true} [:maybe :boolean]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]
   :any])
