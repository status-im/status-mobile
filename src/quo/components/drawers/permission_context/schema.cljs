(ns quo.components.drawers.permission-context.schema)

(def ^:private ?base
  [:map {:closed true}
   [:type [:enum :action :single-token-gating :multiple-token-gating]]
   [:blur? {:optional true} [:maybe :boolean]]
   [:container-style {:optional true} [:maybe :map]]
   [:on-press {:optional true} [:maybe fn?]]])

(def ^:private ?action
  [:map {:closed true}
   [:action-label :string]
   [:action-icon [:qualified-keyword {:namespace :i}]]])

(def ^:private ?token-symbol [:or :keyword :string])

(def ^:private ?single-token-gating
  [:map {:closed true}
   [:token-value :string]
   [:token-symbol ?token-symbol]])

(def ^:private ?multiple-token-gating
  [:map {:closed true}
   [:token-groups
    [:sequential {:min 1} [:sequential {:min 1} ?token-symbol]]]])

(def ?schema
  [:=>
   [:cat
    [:multi {:dispatch :type}
     [:action [:merge ?base ?action]]
     [:single-token-gating [:merge ?base ?single-token-gating]]
     [:multiple-token-gating [:merge ?base ?multiple-token-gating]]]]
   :any])
