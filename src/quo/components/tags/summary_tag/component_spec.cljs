(ns quo.components.tags.summary-tag.component-spec
  (:require
    [quo.components.tags.summary-tag.view :as summary-tag]
    [test-helpers.component :as h]))

(h/describe "Summary tag component tests"
  (h/test "Basic render"
    (h/render [summary-tag/view])
    (h/is-truthy (h/query-by-label-text :container)))

  (h/test "Account view render"
    (h/render [summary-tag/view
               {:type                :account
                :label               "Alice's Account"
                :customization-color "#ff0000"
                :emoji               "🎉"}])
    (h/is-truthy (h/get-by-text "Alice's Account")))

  (h/test "Network view render"
    (h/render [summary-tag/view
               {:type         :network
                :label        "Ethereum Network"
                :image-source "path/to/ethereum-logo.png"}])
    (h/is-truthy (h/get-by-text "Ethereum Network")))

  (h/test "Saved address view render"
    (h/render [summary-tag/view
               {:type                :saved-address
                :label               "Charlie's Wallet"
                :customization-color "#00ff00"}])
    (h/is-truthy (h/get-by-text "Charlie's Wallet")))

  (h/test "Collectible view render"
    (h/render [summary-tag/view
               {:type         :collectible
                :label        "Rare Artifact"
                :image-source "path/to/artifact-image.png"}])
    (h/is-truthy (h/get-by-text "Rare Artifact")))

  (h/test "User view render"
    (h/render-with-theme-provider
     [summary-tag/view
      {:type                :user
       :label               "Bob Smith"
       :image-source        "path/to/profile-pic.png"
       :customization-color "#0000ff"}])
    (h/is-truthy (h/get-by-text "Bob Smith")))

  (h/test "Token view render"
    (h/render [summary-tag/view
               {:type  :token
                :label "1,000 SNT"
                :token :eth}])
    (h/is-truthy (h/get-by-text "1,000 SNT"))))
