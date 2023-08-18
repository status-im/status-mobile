(ns quo2.components.avatars.user-avatar.component-spec
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [test-helpers.component :as h]))

(defonce mock-picture {:uri (js/require "../resources/images/mock2/user_picture_male4.png")})

(h/describe "user avatar"
  (h/describe "Profile picture"
    (h/test "Renders"
      (h/render
       [user-avatar/user-avatar {:profile-picture mock-picture}])
      (h/is-truthy (h/get-by-label-text :profile-picture)))

    (h/test "Renders even if `:full-name` is passed"
      (h/render
       [user-avatar/user-avatar {:profile-picture mock-picture}])
      (h/is-truthy (h/get-by-label-text :profile-picture)))))
