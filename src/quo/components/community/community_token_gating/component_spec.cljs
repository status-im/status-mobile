(ns quo.components.community.community-token-gating.component-spec
  (:require
    [quo.core :as quo]
    [test-helpers.component :as h]))

(h/describe "Community Detail Token Gating Component"
  (h/test "render with satisfied permissions"
    (let [mock-on-press-fn      (h/mock-fn)
          mock-on-press-info-fn (h/mock-fn)]
      (h/render
       [quo/community-token-gating
        {:tokens          [[{:symbol       "ETH"
                             :sufficient?  true
                             :collectible? false
                             :amount       "0.8"
                             :img-src      nil}]
                           [{:symbol       "ETH"
                             :sufficient?  false
                             :collectible? false
                             :amount       "1"
                             :img-src      nil}
                            {:symbol       "STT"
                             :sufficient?  false
                             :collectible? false
                             :amount       "10"
                             :img-src      nil}]]
         :community-color "#FF0000"
         :role            "Member"
         :satisfied?      true
         :on-press        mock-on-press-fn
         :on-press-info   mock-on-press-info-fn}])
      (h/is-truthy (h/get-by-translation-text :t/you-eligible-to-join-as {:role "Member"}))
      (h/is-truthy (h/get-by-text "0.8 ETH"))
      (h/is-truthy (h/get-by-text "1 ETH"))
      (h/is-truthy (h/get-by-text "10 STT"))
      (h/fire-event :press (h/get-by-translation-text :t/request-to-join))
      (h/was-called mock-on-press-fn)
      (h/fire-event :press (h/get-by-label-text :community-token-gating-info))
      (h/was-called mock-on-press-info-fn)))

  (h/test "render with unsatisfied permissions"
    (let [mock-on-press-fn      (h/mock-fn)
          mock-on-press-info-fn (h/mock-fn)]
      (h/render
       [quo/community-token-gating
        {:tokens          [[{:symbol       "ETH"
                             :sufficient?  false
                             :collectible? false
                             :amount       "0.8"
                             :img-src      nil}]]
         :community-color "#FF0000"
         :role            "Member"
         :satisfied?      false
         :on-press        mock-on-press-fn
         :on-press-info   mock-on-press-info-fn}])
      (h/is-truthy (h/get-by-translation-text :t/you-not-eligible-to-join))
      (h/is-truthy (h/get-by-text "0.8 ETH"))
      (h/fire-event :press (h/get-by-translation-text :t/request-to-join))
      (h/was-not-called mock-on-press-fn)
      (h/fire-event :press (h/get-by-label-text :community-token-gating-info))
      (h/was-called mock-on-press-info-fn))))
