(ns quo.components.avatars.wallet-user-avatar.component-spec
  (:require [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
            [test-helpers.component :as h]))

(h/describe "wallet user avatar"
  (h/describe "View internal"
    (h/test "Renders by default even with no input parameters"
      (h/render
       [wallet-user-avatar/wallet-user-avatar {}])
      (h/is-truthy (h/query-by-label-text :wallet-user-avatar)))

    (h/test "Renders user’s initials when full name is provided"
      (h/render
       [wallet-user-avatar/wallet-user-avatar {:full-name "Jane Smith"}])
      (h/is-truthy (h/get-by-text "JS")))))

