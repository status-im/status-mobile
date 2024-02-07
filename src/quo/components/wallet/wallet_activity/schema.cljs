(ns quo.components.wallet.wallet-activity.schema
  (:require [quo.components.tags.context-tag.schema :as ?context-tag]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:transaction {:optional true} [:maybe [:enum :receive :send :swap :bridge :buy :destroy :mint]]]
      [:status {:optional true} [:maybe [:enum :pending :confirmed :finalised :failed]]]
      [:counter {:optional true} [:maybe :int]]
      [:timestamp {:optional true} [:maybe :string]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:on-press {:optional true} [:maybe fn?]]
      [:state {:optional true} [:maybe [:enum nil :disabled]]]
      [:theme :schema.common/theme]
      [:second-tag-prefix {:optional true} [:maybe :keyword]]
      [:third-tag-prefix {:optional true} [:maybe :keyword]]
      [:fourth-tag-prefix {:optional true} [:maybe :keyword]]
      [:first-tag {:optional true} [:maybe ?context-tag/?schema]]
      [:second-tag {:optional true} [:maybe ?context-tag/?schema]]
      [:third-tag {:optional true} [:maybe ?context-tag/?schema]]
      [:fourth-tag {:optional true} [:maybe ?context-tag/?schema]]]]]
   :any])