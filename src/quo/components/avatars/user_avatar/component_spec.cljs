(ns quo.components.avatars.user-avatar.component-spec
  (:require
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [test-helpers.component :as h]))

(defonce mock-picture 1)

(h/describe "user avatar"
  (h/describe "Profile picture"
    (h/test "Renders"
      (h/render-with-theme-provider
       [user-avatar/user-avatar {:profile-picture mock-picture}])
      (h/is-truthy (h/get-by-label-text :profile-picture)))

    (h/test "Renders even if `:full-name` is passed"
      (h/render-with-theme-provider
       [user-avatar/user-avatar {:profile-picture mock-picture}])
      (h/is-truthy (h/get-by-label-text :profile-picture)))))
