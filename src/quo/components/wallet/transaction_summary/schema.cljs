(ns quo.components.wallet.transaction-summary.schema
  (:require [quo.components.tags.context-tag.schema :as context-tag-schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:schema.common/map {:optional true :maybe true}
      [:transaction [:enum :send :swap :bridge]]
      [:first-tag context-tag-schema/?schema]
      [:second-tag context-tag-schema/?schema]
      [:third-tag context-tag-schema/?schema]
      [:fourth-tag context-tag-schema/?schema]
      [:fifth-tag context-tag-schema/?schema]
      [:second-tag-prefix :keyword]
      [:third-tag-prefix :keyword]
      [:fourth-tag-prefix :keyword]
      [:max-fees :string]
      [:nonce :int]
      [:input-data :string]
      [:on-press fn?]]]]
   :any])
