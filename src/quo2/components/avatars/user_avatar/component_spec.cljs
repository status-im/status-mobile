(ns quo2.components.avatars.user-avatar.component-spec
  (:require  [quo2.components.avatars.user-avatar.view :as user-avatar]
             [test-helpers.component :as h]))



(js/jest.mock "react-native-fast-image" (fn []
                                          (fn [props] props.source.uri)))


(h/describe "user avatar"
            (h/test "it tests the default render"
                    (h/render [user-avatar/user-avatar])
                    (-> (js/expect (h/get-by-text "EN"))
                        (.toBeTruthy)))

            (h/test "it tests using a different name "
                    (h/render [user-avatar/user-avatar {:full-name "Test User"}])
                    (-> (js/expect (h/get-by-text "TU"))
                        (.toBeTruthy)))

            (h/test "it tests using a custom picture "
                    (h/render [user-avatar/user-avatar {:full-name "Test User"
                                                        :profile-picture "some mock uri"}])
                    (-> (js/expect (h/find-by-text "some mock uri"))
                        (.toBeTruthy))))
