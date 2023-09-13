(ns quo2.components.avatars.wallet-user-avatar.component-spec
  (:require [quo2.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
            [test-helpers.component :as h]))

(h/describe "wallet user avatar"
  (h/describe "View internal"
    (h/test "Renders with default values"
      (h/render
       [wallet-user-avatar/view {}])
      (h/is-truthy (h/query-by-label-text :wallet-user-avatar)))
    (h/test "Renders with given parameters"
      (h/render
       [wallet-user-avatar/view
        {:full-name "John Doe"
         :color     :blue
         :size      :medium}])
      (h/is-truthy (h/query-by-label-text :wallet-user-avatar)))
    (h/test "Renders initials correctly"
      (h/render
       [wallet-user-avatar/view {:full-name "Jane Smith"}])
      (h/is-truthy (h/get-by-text "JS")))))
