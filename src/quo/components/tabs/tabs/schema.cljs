(ns quo.components.tabs.tabs.schema)

(def ^:private ?data
  [:sequential
   [:maybe
    [:map
     [:id [:or :int :keyword [:set :int]]]
     [:label [:maybe :string]]
     [:accessibility-label {:optional true} [:maybe [:or :keyword :string]]]
     [:notification-dot? {:optional true} [:maybe :boolean]]]]])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:default-active {:optional true} [:maybe [:or :int :keyword]]]
      [:active-tab-id {:optional true} [:maybe [:or :int :keyword]]]
      [:data ?data]
      [:fade-end-percentage {:optional true} [:or :double :string]]
      [:fade-end? {:optional true} [:maybe :boolean]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:on-change {:optional true} [:maybe fn?]]
      [:on-scroll {:optional true} [:maybe fn?]]
      [:scroll-on-press? {:optional true} [:maybe :boolean]]
      [:scrollable? {:optional true} [:maybe :boolean]]
      [:style {:optional true} [:maybe :map]]
      [:container-style {:optional true} [:maybe :map]]
      [:size {:optional true} [:maybe [:or :keyword :int]]]
      [:in-scroll-view? {:optional true} [:maybe :boolean]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]]
   :any])
