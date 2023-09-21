(ns quo2.components.selectors.disclaimer.component-spec
  (:require [quo2.components.selectors.disclaimer.view :as disclaimer]
            [test-helpers.component :as h]))

(h/describe "Disclaimer tests"
  (h/test "Default render of toggle component"
    (h/render [disclaimer/view {:on-change (h/mock-fn)} "test"])
    (h/is-truthy (h/get-by-label-text :checkbox-off)))

  (h/test "Renders its text"
    (let [text "I accept this disclaimer"]
      (h/render [disclaimer/view {} text])
      (h/is-truthy (h/get-by-text text))))

  (h/test "On change event gets fire after press"
    (let [mock-fn (h/mock-fn)]
      (h/render [disclaimer/view {:on-change mock-fn} "test"])
      (h/fire-event :press (h/get-by-label-text :checkbox-off))
      (h/was-called mock-fn)))

  (h/describe "It's rendered according to its `checked?` property"
    (h/test "checked? true"
      (h/render [disclaimer/view {:checked? true} "test"])
      (h/is-null (h/query-by-label-text :checkbox-off))
      (h/is-truthy (h/query-by-label-text :checkbox-on)))
    (h/test "checked? false"
      (h/render [disclaimer/view {:checked? false} "test"])
      (h/is-null (h/query-by-label-text :checkbox-on))
      (h/is-truthy (h/query-by-label-text :checkbox-off)))))
