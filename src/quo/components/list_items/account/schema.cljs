(ns quo.components.list-items.account.schema)

(def ^:private ?balance
  [:map
   [:balance-props
    [:map
     [:fiat-value :string]
     [:percentage-change :string]
     [:fiat-change :string]]]])

(def ^:private ?tag
  [:map
   [:token-props
    [:map
     [:symbol :string]
     [:value :string]]]])

(def ^:private ?action
  [:map
   [:on-options-press {:optional true} [:maybe fn?]]])

(def ^:private ?base
  [:map
   [:type {:optional true}
    [:enum :default :tag :action :balance-neutral :balance-negative :balance-positive]]
   [:state {:optional true} [:enum :default :selected :active :disabled]]
   [:blur? {:optional true} [:maybe :boolean]]
   [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
   [:on-press {:optional true} [:maybe fn?]]
   [:title-icon {:optional true} [:maybe :keyword]]
   [:account-props
    [:map
     [:name :string]
     [:address :string]
     [:emoji :string]
     [:customization-color {:optional true} [:maybe :schema.common/customization-color]]]]])

(def ?schema
  [:=>
   [:cat
    [:multi {:dispatch :type}
     [:balance-neutral [:merge ?base ?balance]]
     [:balance-negative [:merge ?base ?balance]]
     [:balance-positive [:merge ?base ?balance]]
     [:tag [:merge ?base ?tag]]
     [:action [:merge ?base ?action]]
     [:default ?base]
     [:malli.core/default ?base]]]
   :any])
