(ns quo.components.avatars.token-avatar.component-spec
  (:require [quo.components.avatars.token-avatar.view :as token-avatar]
            [test-helpers.component :as h]))

(h/describe "Avatars: Token Avatar"
  (h/test "should render correctly without context"
    (h/render-with-theme-provider
     [token-avatar/view {:image "mock-image"}])
    (h/is-truthy (h/get-by-label-text :token-avatar)))

  (h/test "should render correctly with context"
    (h/render-with-theme-provider
     [token-avatar/view {:image "mock-image" :network-image "mock-image" :context? true}])
    (h/is-truthy (h/get-by-label-text :token-avatar))))
