(ns quo.components.community.community-token-gating.schema)

(def ^:private ?token-schema
  [:map
   [:symbol :string]
   [:sufficient? :boolean]
   [:collectible? :boolean]
   [:amount {:optional true} [:maybe :string]]
   [:img-src {:optional true} [:maybe :schema.common/image-source]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:tokens [:sequential [:sequential ?token-schema]]]
      [:community-color [:or :string :schema.common/customization-color]]
      [:role {:optional true} [:maybe :string]]
      [:satisfied? :boolean]
      [:on-press fn?]
      [:on-press-info fn?]]]]
   :any])
