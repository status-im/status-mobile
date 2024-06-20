(ns quo.components.list-items.account-list-card.schema)

(def ^:private ?base
  [:schema.common/map {:optional true}
   [:action [:enum :icon :none]]
   [:blur? [:maybe :boolean]]
   [:on-press [:maybe fn?]]
   [:account-props {:no-optional true}
    [:map {:closed true}
     [:type [:enum :default :watch-only]]
     [:name :string]
     [:address :string]
     [:emoji :string]
     [:size {:optional true} [:enum 80 :size-64 48 32 28 24 20 16]]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]
   [:networks [:* [:map [:network-name :keyword] [:short-name :string]]]]])

(def ^:private ?on-option-press
  [:map
   [:on-options-press [:maybe fn?]]])

(def ?schema
  [:=>
   [:cat
    [:multi {:dispatch :action}
     [:icon [:merge ?base ?on-option-press]]
     [:none ?base]
     [:malli.core/default ?base]]]
   :any])
