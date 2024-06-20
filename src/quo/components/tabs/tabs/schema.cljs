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
     [:schema.common/map {:optional true :maybe true}
      [:default-active [:or :int :keyword]]
      [:active-tab-id [:or :int :keyword]]
      [:data {:no-maybe true} ?data]
      [:fade-end-percentage {:no-maybe true} [:or :double :string]]
      [:fade-end? :boolean]
      [:blur? :boolean]
      [:on-change fn?]
      [:on-scroll fn?]
      [:scroll-on-press? :boolean]
      [:scrollable? :boolean]
      [:style :map]
      [:container-style :map]
      [:size [:or :keyword :int]]
      [:in-scroll-view? :boolean]
      [:customization-color :schema.common/customization-color]]]]
   :any])
