(ns quo2.components.avatars.user-avatar.component-spec
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [test-helpers.component :as h]))

(defonce mock-picture (js/require "../resources/images/mock2/user_picture_male4.png"))

(h/describe "user avatar"
  (h/test "Default render"
    (h/render [user-avatar/user-avatar])
    (h/is-truthy (h/get-by-label-text :user-avatar))
    (h/is-truthy (h/get-by-text "EN")))

  (h/describe "Profile picture"
    (h/test "Renders"
      (h/render
       [user-avatar/user-avatar {:profile-picture mock-picture}])
      (h/is-truthy (h/get-by-label-text :profile-picture)))

    (h/test "Renders even if `:full-name` is passed"
      (h/render
       [user-avatar/user-avatar
        {:profile-picture mock-picture
         :full-name       "New User1"}])
      (h/is-truthy (h/get-by-label-text :profile-picture))
      (h/is-null (h/query-by-label-text :initials-avatar)))

    (h/describe "Status indicator"
      (h/test "Render"
        (h/render
         [user-avatar/user-avatar
          {:profile-picture   mock-picture
           :status-indicator? true}])
        (h/is-truthy (h/get-by-label-text :profile-picture))
        (h/is-truthy (h/get-by-label-text :status-indicator)))

      (h/test "Do not render"
        (h/render
         [user-avatar/user-avatar
          {:profile-picture   mock-picture
           :status-indicator? false}])
        (h/is-truthy (h/get-by-label-text :profile-picture))
        (h/is-null (h/query-by-label-text :status-indicator)))))

  (h/describe "Initials Avatar"
    (h/describe "Render initials"
      (letfn [(user-avatar-component [size]
                [user-avatar/user-avatar
                 {:full-name "New User"
                  :size      size}])]
        (h/describe "Two letters"
          (h/test "Size :big"
            (h/render (user-avatar-component :big))
            (h/is-truthy (h/get-by-text "NU")))

          (h/test "Size :medium"
            (h/render (user-avatar-component :medium))
            (h/is-truthy (h/get-by-text "NU")))

          (h/test "Size :small"
            (h/render (user-avatar-component :small))
            (h/is-truthy (h/get-by-text "NU"))))

        (h/describe "One letter"
          (h/test "Size :xs"
            (h/render (user-avatar-component :xs))
            (h/is-truthy (h/get-by-text "N")))

          (h/test "Size :xxs"
            (h/render (user-avatar-component :xxs))
            (h/is-truthy (h/get-by-text "N")))

          (h/test "Size :xxxs"
            (h/render (user-avatar-component :xxxs))
            (h/is-truthy (h/get-by-text "N"))))))

    (h/describe "Render ring"
      (letfn [(user-avatar-component [size]
                [user-avatar/user-avatar
                 {:full-name       "New User"
                  :ring-background mock-picture
                  :size            size}])]
        (h/describe "Passed and drawn"
          (h/test "Size :big"
            (h/render (user-avatar-component :big))
            (h/is-truthy (h/get-by-label-text :initials-avatar))
            (h/is-truthy (h/get-by-label-text :ring-background)))

          (h/test "Size :medium"
            (h/render (user-avatar-component :medium))
            (h/is-truthy (h/get-by-label-text :initials-avatar))
            (h/is-truthy (h/get-by-label-text :ring-background)))

          (h/test "Size :small"
            (h/render (user-avatar-component :small))
            (h/is-truthy (h/get-by-label-text :initials-avatar))
            (h/is-truthy (h/get-by-label-text :ring-background))))

        (h/describe "Passed and not drawn (because of invalid size for ring)"
          (h/test "Size :xs"
            (h/render (user-avatar-component :xs))
            (h/is-truthy (h/get-by-label-text :initials-avatar))
            (h/is-null (h/query-by-label-text :ring-background)))

          (h/test "Size :xxs"
            (h/render (user-avatar-component :xxs))
            (h/is-truthy (h/get-by-label-text :initials-avatar))
            (h/is-null (h/query-by-label-text :ring-background)))

          (h/test "Size :xxxs"
            (h/render (user-avatar-component :xxxs))
            (h/is-truthy (h/get-by-label-text :initials-avatar))
            (h/is-null (h/query-by-label-text :ring-background))))))

    (h/describe "Status indicator"
      (h/test "Render"
        (h/render
         [user-avatar/user-avatar
          {:full-name         "Test User"
           :status-indicator? true}])
        (h/is-truthy (h/get-by-label-text :initials-avatar))
        (h/is-truthy (h/get-by-label-text :status-indicator)))

      (h/test "Do not render"
        (h/render
         [user-avatar/user-avatar
          {:full-name         "Test User"
           :status-indicator? false}])
        (h/is-truthy (h/get-by-label-text :initials-avatar))
        (h/is-null (h/query-by-label-text :status-indicator))))))
