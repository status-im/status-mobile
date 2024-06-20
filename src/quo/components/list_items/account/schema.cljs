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
  [:schema.common/map {:optional true}
   [:type [:enum :default :tag :action :balance-neutral :balance-negative :balance-positive]]
   [:state [:enum :default :selected :active]]
   [:blur? [:maybe :boolean]]
   [:customization-color [:maybe :schema.common/customization-color]]
   [:on-press [:maybe fn?]]
   [:title-icon [:maybe :keyword]]
   [:account-props {:no-optional true}
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
