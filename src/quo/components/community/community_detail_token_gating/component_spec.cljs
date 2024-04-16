(ns quo.components.community.community-detail-token-gating.component-spec
  (:require
    [quo.core :as quo]
    [test-helpers.component :as h]))

(h/describe "Community Detail Token Gating Component"
  (h/test "render with member permissions"
    (h/render
     [quo/community-detail-token-gating
      {:permissions [{:role       2
                      :role-text  "Member"
                      :satisfied? true
                      :tokens     [[{:symbol       "ETH"
                                     :sufficient?  true
                                     :collectible? false
                                     :amount       0.8
                                     :img-src      nil}]
                                   [{:symbol       "ETH"
                                     :sufficient?  false
                                     :collectible? false
                                     :amount       1
                                     :img-src      nil}
                                    {:symbol       "STT"
                                     :sufficient?  false
                                     :collectible? false
                                     :amount       10
                                     :img-src      nil}]]}]}])
    (h/is-truthy (h/get-by-translation-text :t/you-eligible-to-join-as {:role "Member"}))
    (h/is-truthy (h/get-by-text "0.8 ETH"))
    (h/is-truthy (h/get-by-text "1 ETH"))
    (h/is-truthy (h/get-by-text "10 STT")))

  (h/test "render with admin permissions"
    (h/render
     [quo/community-detail-token-gating
      {:permissions [{:role       1
                      :role-text  "Admin"
                      :satisfied? true
                      :tokens     [[{:symbol       "ETH"
                                     :sufficient?  true
                                     :collectible? false
                                     :amount       2
                                     :img-src      nil}]]}]}])
    (h/is-truthy (h/get-by-translation-text :t/you-eligible-to-join-as {:role "Admin"}))
    (h/is-truthy (h/get-by-text "2 ETH")))

  (h/test "render with token master permissions"
    (h/render-with-theme-provider
     [quo/community-detail-token-gating
      {:permissions [{:role       5
                      :role-text  "Token Master"
                      :satisfied? true
                      :tokens     [[{:symbol       "TMANI "
                                     :sufficient?  true
                                     :collectible? true
                                     :img-src      {:uri "mock-image"}}]]}]}])
    (h/is-truthy (h/get-by-translation-text :t/you-eligible-to-join-as {:role "Token Master"}))
    (h/is-truthy (h/get-by-text "TMANI")))

  (h/test "render with token owner permissions"
    (h/render-with-theme-provider
     [quo/community-detail-token-gating
      {:permissions [{:role       6
                      :role-text  "Token Owner"
                      :satisfied? true
                      :tokens     [[{:symbol       "TOANI"
                                     :sufficient?  true
                                     :collectible? true
                                     :img-src      {:uri "mock-image"}}]]}]}])
    (h/is-truthy (h/get-by-translation-text :t/you-eligible-to-join-as {:role "Token Owner"}))
    (h/is-truthy (h/get-by-text "TOANI"))))
