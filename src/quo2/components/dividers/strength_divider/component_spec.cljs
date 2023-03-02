(ns quo2.components.dividers.strength-divider.component-spec
  (:require [quo2.components.dividers.strength-divider.view :as strength-divider]
            [test-helpers.component :as h]))

(h/describe "select-profile component"
  (h/test "render component"
    (h/render [strength-divider/view {:type :okay}])
    (-> (h/expect (h/get-by-label-text :strength-divider))
        (.toBeTruthy)))
  (h/test "render component with :type :very-weak"
    (h/render [strength-divider/view {:type :very-weak}])
    (-> (h/expect (h/get-by-translation-text :strength-divider-very-weak-label))
        (.toBeTruthy)))
  (h/test "render component with :type :weak"
    (h/render [strength-divider/view {:type :weak}])
    (-> (h/expect (h/get-by-translation-text :strength-divider-weak-label))
        (.toBeTruthy)))
  (h/test "render component with :type :okay"
    (h/render [strength-divider/view {:type :okay}])
    (-> (h/expect (h/get-by-translation-text :strength-divider-okay-label))
        (.toBeTruthy)))
  (h/test "render component with :type :strong"
    (h/render [strength-divider/view {:type :strong}])
    (-> (h/expect (h/get-by-translation-text :strength-divider-strong-label))
        (.toBeTruthy)))
  (h/test "render component with :type :very-strong"
    (h/render [strength-divider/view {:type :very-strong}])
    (-> (h/expect (h/get-by-translation-text :strength-divider-very-strong-label))
        (.toBeTruthy)))
  (h/test "render component with :type :alert"
    (h/render [strength-divider/view {:type :alert} "Text message"])
    (-> (h/expect (h/get-by-text "Text message"))
        (.toBeTruthy)))
  (h/test "render component with :type :info"
    (h/render [strength-divider/view {:type :info} "Text Info"])
    (-> (h/expect (h/get-by-text "Text Info"))
        (.toBeTruthy))))

