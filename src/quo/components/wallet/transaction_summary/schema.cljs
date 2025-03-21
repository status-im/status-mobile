(ns quo.components.wallet.transaction-summary.schema
  (:require [quo.components.tags.context-tag.schema :as context-tag-schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map
      [:transaction {:optional true} [:maybe [:enum :send :swap :bridge]]]
      [:first-tag {:optional true} [:maybe context-tag-schema/?schema]]
      [:second-tag {:optional true} [:maybe context-tag-schema/?schema]]
      [:third-tag {:optional true} [:maybe context-tag-schema/?schema]]
      [:fourth-tag {:optional true} [:maybe context-tag-schema/?schema]]
      [:fifth-tag {:optional true} [:maybe context-tag-schema/?schema]]
      [:second-tag-prefix {:optional true} [:maybe :keyword]]
      [:third-tag-prefix {:optional true} [:maybe :keyword]]
      [:fourth-tag-prefix {:optional true} [:maybe :keyword]]
      [:max-fees {:optional true} [:maybe :string]]
      [:nonce {:optional true} [:maybe :int]]
      [:input-data {:optional true} [:maybe :string]]
      [:on-press {:optional true} [:maybe fn?]]]]]
   :any])
