(ns quo2.components.profile.select-profile.component-spec
  (:require [quo2.components.profile.select-profile.view :as select-profile]
            [test-helpers.component :as h]))

(h/describe "select-profile component"
  (h/test "render component"
    (h/render [select-profile/view])
    (-> (h/expect (h/get-by-label-text :select-profile))
        (.toBeTruthy)))
  (h/test "call on-change handler when clicked"
    (let [on-change (h/mock-fn)]
      (h/render [select-profile/view {:on-change on-change}])
      (h/fire-event :on-press (h/get-by-label-text :select-profile))
      (-> (h/expect on-change)
          (.toHaveBeenCalledTimes 1)))))

