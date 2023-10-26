(ns quo.components.counter.step.component-spec
  (:require
    [quo.components.counter.step.view :as step]
    quo.theme
    [test-helpers.component :as h]))

(defn render
  [component]
  (h/render-with-theme-provider component :dark))

(h/describe "step component"
  (h/test "default render of step component"
    (render [step/view {} nil])
    (-> (h/expect (h/query-by-label-text :step-counter))
        (h/is-truthy)))

  (h/test "renders step with a string value"
    (render [step/view {} "1"])
    (-> (h/expect (h/get-by-text "1"))
        (h/is-truthy)))

  (h/test "renders step with an integer value"
    (render [step/view {} 1])
    (-> (h/expect (h/get-by-text "1"))
        (h/is-truthy))))
