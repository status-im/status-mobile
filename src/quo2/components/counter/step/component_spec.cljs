(ns quo2.components.counter.step.component-spec
  (:require [quo2.components.counter.step.view :as step]
            [test-helpers.component :as h]))

(h/describe "step component"
  (h/test "default render of step component"
    (h/render [step/view {} nil])
    (-> (h/expect (h/query-by-label-text :step-counter))
        (h/is-truthy)))

  (h/test "renders step with a string value"
    (h/render [step/view {} "1"])
    (-> (h/expect (h/get-by-text "1"))
        (h/is-truthy)))

  (h/test "renders step with an integer value"
    (h/render [step/view {} 1])
    (-> (h/expect (h/get-by-text "1"))
        (h/is-truthy))))
