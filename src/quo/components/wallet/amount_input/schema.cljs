(ns quo.components.wallet.amount-input.schema)

(def return-key-types
  [:enum :done :go :next :search :send :none :previous :default
   :emergency-call :google :join :route :yahoo])

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:status {:optional true} [:maybe [:enum :default :error]]]
      [:theme :schema.common/theme]
      [:on-change-text {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :map]]
      [:auto-focus? {:optional true} [:maybe :boolean]]
      [:min-value {:optional true} [:maybe :int]]
      [:max-value {:optional true} [:maybe :int]]
      [:return-key-type {:optional true} [:maybe return-key-types]]
      [:init-value {:optional true} [:maybe :int]]]]]
   :any])
