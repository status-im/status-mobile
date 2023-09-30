(ns quo2.components.avatars.collection-avatar.component-spec
  (:require [quo2.components.avatars.collection-avatar.view :as collection-avatar]
            [status-im2.common.resources :as resources]
            [test-helpers.component :as h]))

(defonce bored-ape (resources/get-mock-image :bored-ape))

(h/describe "collection avatar"
  (h/describe "Profile picture"
    (h/test "Doesn't crash without image"
      (h/render
       [collection-avatar/view])
      (h/is-truthy (h/get-by-label-text :collection-avatar)))

    (h/test "Renders with image"
      (h/render
       [collection-avatar/view {:image bored-ape}])
      (h/is-truthy (h/get-by-label-text :collection-avatar)))))
