(ns quo.components.text-combinations.channel-name.component-spec
  (:require [quo.components.text-combinations.channel-name.view :as channel-name]
            [test-helpers.component :as h]))

(h/describe "Channel name"
  (h/test "Renders Default"
    (h/render [channel-name/view {:channel-name "Test channel"}])
    (h/is-truthy (h/get-by-text "# Test channel")))

  (h/test "Renders unlocked icon"
    (h/render [channel-name/view
               {:channel-name "Test channel"
                :unlocked?    true}])
    (h/is-truthy (h/get-by-label-text :channel-name-unlocked-icon)))

  (h/test "Renders muted icon"
    (h/render [channel-name/view
               {:channel-name "Test channel"
                :muted?       true}])
    (h/is-truthy (h/get-by-label-text :channel-name-muted-icon)))

  (h/test "Renders muted and unlocked icon"
    (h/render [channel-name/view
               {:channel-name "Test channel"
                :muted?       true
                :unlocked?    true}])
    (h/is-truthy (h/get-by-label-text :channel-name-unlocked-icon))
    (h/is-truthy (h/get-by-label-text :channel-name-muted-icon))))
