(ns quo2.components.avatars.user-avatar.component-spec
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [status-im2.common.resources :as resources]
            [test-helpers.component :as h]))

(defn- test-truthy [element]
  (.toBeTruthy (js/expect element)))

(defn- test-null [element]
  (.toBeNull (js/expect element)))

(h/describe "user avatar"
  (h/test "Default render"
    (h/render [user-avatar/user-avatar])
    (test-truthy (h/get-by-label-text :user-avatar))
    (test-truthy (h/get-by-text "EN")))

  (h/describe "Profile picture"
    (h/test "Renders"
      (h/render
       [user-avatar/user-avatar {:profile-picture (resources/get-mock-image :user-picture-male4)}])
      (test-truthy (h/get-by-label-text :profile-picture)))

    (h/test "Renders even if `:full-name` is passed"
      (h/render
       [user-avatar/user-avatar {:profile-picture (resources/get-mock-image :user-picture-male4)
                                 :full-name       "New User1"}])
      (test-truthy (h/get-by-label-text :profile-picture))
      (test-null (h/query-by-label-text :initials-avatar)))

    (h/describe "Status indicator"
      (h/test "Render"
        (h/render
         [user-avatar/user-avatar {:profile-picture   (resources/get-mock-image :user-picture-male4)
                                   :status-indicator? true}])
        (test-truthy (h/get-by-label-text :profile-picture))
        (test-truthy (h/get-by-label-text :status-indicator)))

      (h/test "Do not render"
        (h/render
         [user-avatar/user-avatar {:profile-picture   (resources/get-mock-image :user-picture-male4)
                                   :status-indicator? false}])
        (test-truthy (h/get-by-label-text :profile-picture))
        (test-null (h/query-by-label-text :status-indicator)))))

  (h/describe "Initials Avatar"
    (h/describe "Render initials"
      (letfn [(user-avatar-component [size]
                [user-avatar/user-avatar {:full-name "New User"
                                          :size      size}])]
        (h/describe "Two letters"
          (h/test "Size :big"
            (h/render (user-avatar-component :big))
            (test-truthy (h/get-by-text "NU")))

          (h/test "Size :medium"
            (h/render (user-avatar-component :medium))
            (test-truthy (h/get-by-text "NU")))

          (h/test "Size :small"
            (h/render (user-avatar-component :small))
            (test-truthy (h/get-by-text "NU"))))

        (h/describe "One letter"
          (h/test "Size :xs"
            (h/render (user-avatar-component :xs))
            (test-truthy (h/get-by-text "N")))

          (h/test "Size :xxs"
            (h/render (user-avatar-component :xxs))
            (test-truthy (h/get-by-text "N")))

          (h/test "Size :xxxs"
            (h/render (user-avatar-component :xxxs))
            (test-truthy (h/get-by-text "N"))))))

    (h/describe "Render ring"
      (letfn [(user-avatar-component [size]
                [user-avatar/user-avatar {:full-name       "New User"
                                          :ring-background (resources/get-mock-image :ring)
                                          :size            size}])]
        (h/describe "Passed and drawn"
          (h/test "Size :big"
            (h/render (user-avatar-component :big))
            (test-truthy (h/get-by-label-text :initials-avatar))
            (test-truthy (h/get-by-label-text :ring-background)))

          (h/test "Size :medium"
            (h/render (user-avatar-component :medium))
            (test-truthy (h/get-by-label-text :initials-avatar))
            (test-truthy (h/get-by-label-text :ring-background)))

          (h/test "Size :small"
            (h/render (user-avatar-component :small))
            (test-truthy (h/get-by-label-text :initials-avatar))
            (test-truthy (h/get-by-label-text :ring-background))))

        (h/describe "Passed and not drawn (because of invalid size for ring)"
          (h/test "Size :xs"
            (h/render (user-avatar-component :xs))
            (test-truthy (h/get-by-label-text :initials-avatar))
            (test-null (h/query-by-label-text :ring-background)))

          (h/test "Size :xxs"
            (h/render (user-avatar-component :xxs))
            (test-truthy (h/get-by-label-text :initials-avatar))
            (test-null (h/query-by-label-text :ring-background)))

          (h/test "Size :xxxs"
            (h/render (user-avatar-component :xxxs))
            (test-truthy (h/get-by-label-text :initials-avatar))
            (test-null (h/query-by-label-text :ring-background))))))

    (h/describe "Status indicator"
      (h/test "Render"
        (h/render
         [user-avatar/user-avatar {:full-name         "Test User"
                                   :status-indicator? true}])
        (test-truthy (h/get-by-label-text :initials-avatar))
        (test-truthy (h/get-by-label-text :status-indicator)))

      (h/test "Do not render"
        (h/render
         [user-avatar/user-avatar {:full-name         "Test User"
                                   :status-indicator? false}])
        (test-truthy (h/get-by-label-text :initials-avatar))
        (test-null (h/query-by-label-text :status-indicator))))))
