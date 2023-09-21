(ns quo2.components.password.tips.component-spec
  (:require [quo2.components.password.tips.view :as quo]
            [test-helpers.component :as h]))

(h/describe "password tips component"
  (h/test "render component"
    (h/render [quo/view])
    (-> (h/expect (h/get-by-label-text :password-tips))
        (.toBeTruthy)))
  (h/test "render component with proper text"
    (h/render [quo/view {:completed? false} "Upper case"])
    (-> (h/expect (h/get-by-text "Upper case"))
        (.toBeTruthy)))
  (h/test "render component with completed proper text & completed state"
    (h/render [quo/view {:completed? true} "Numbers"])
    (-> (h/expect (h/get-by-text "Numbers"))
        (.toBeTruthy))
    (-> (h/expect (h/get-by-label-text :password-tips-completed))
        (.toBeTruthy))))

