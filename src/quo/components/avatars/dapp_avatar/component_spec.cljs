(ns quo.components.avatars.dapp-avatar.component-spec
  (:require [quo.components.avatars.dapp-avatar.view :as dapp-avatar]
            [test-helpers.component :as h]))

(h/describe "Avatars: dApp Avatar"
  (h/test "should render correctly without context"
    (h/render-with-theme-provider
     [dapp-avatar/view {:image "mock-image"}])
    (h/is-truthy (h/get-by-label-text :dapp-avatar)))

  (h/test "should render correctly with context"
    (h/render-with-theme-provider
     [dapp-avatar/view {:image "mock-image" :network-image "mock-image" :context? true}])
    (h/is-truthy (h/get-by-label-text :dapp-avatar))))
