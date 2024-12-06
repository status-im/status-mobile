(ns quo.components.dividers.strength-divider.component-spec
  (:require
    [quo.components.dividers.strength-divider.view :as strength-divider]
    [test-helpers.component :as h]))

(h/describe "select-profile component"
  (h/test "render component"
    (h/render [strength-divider/view {:type :okay}])
    (h/is-truthy (h/get-by-label-text :strength-divider)))
  (h/test "render component with :type :very-weak"
    (h/render [strength-divider/view {:type :very-weak}])
    (h/is-truthy (h/get-by-translation-text :t/strength-divider-very-weak-label)))
  (h/test "render component with :type :weak"
    (h/render [strength-divider/view {:type :weak}])
    (h/is-truthy (h/get-by-translation-text :t/strength-divider-weak-label)))
  (h/test "render component with :type :okay"
    (h/render [strength-divider/view {:type :okay}])
    (h/is-truthy (h/get-by-translation-text :t/strength-divider-okay-label)))
  (h/test "render component with :type :strong"
    (h/render [strength-divider/view {:type :strong}])
    (h/is-truthy (h/get-by-translation-text :t/strength-divider-strong-label)))
  (h/test "render component with :type :very-strong"
    (h/render [strength-divider/view {:type :very-strong}])
    (h/is-truthy (h/get-by-translation-text :t/strength-divider-very-strong-label)))
  (h/test "render component with :type :alert"
    (h/render [strength-divider/view {:type :alert} "Text message"])
    (h/is-truthy (h/get-by-text "Text message")))
  (h/test "render component with :type :info"
    (h/render [strength-divider/view {:type :info} "Text Info"])
    (h/is-truthy (h/get-by-text "Text Info"))))
