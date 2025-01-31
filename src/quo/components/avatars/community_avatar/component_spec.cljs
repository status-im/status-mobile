(ns quo.components.avatars.community-avatar.component-spec
  (:require [quo.components.avatars.community-avatar.view :as community-avatar]
            [test-helpers.component :as h]))

(h/describe "Avatars: Community Avatar"
  (h/test "should render correctly"
    (h/render-with-theme-provider
     [community-avatar/view {:image "mock-image"}])
    (h/is-truthy (h/get-by-label-text :community-avatar))))
