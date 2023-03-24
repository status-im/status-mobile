(ns quo2.components.selectors.disclaimer.component-spec
  (:require [quo2.components.selectors.disclaimer.view :as disclaimer]
            [test-helpers.component :as h]))

(h/describe "disclaimer tests"
            (h/test "default render of toggle component"
                    (h/render [disclaimer/view {:on-change (h/mock-fn)} "test"])
                    (h/is-truthy (h/get-by-label-text :checkbox-off)))

            (h/test "on change event gets fire after press"
                    (let [mock-fn (h/mock-fn)]
                      (h/debug (h/render [disclaimer/view {:on-change mock-fn} "test"]))
                      (h/fire-event :press (h/get-by-label-text :checkbox-off))
                      (h/was-called mock-fn))))

