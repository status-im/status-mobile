(ns quo2.components.inputs.locked-input.component-spec
  (:require [quo2.components.inputs.locked-input.view :as locked-input]
            [test-helpers.component :as h]))

(h/describe "Locked Input"
            (h/test "renders label, value and icon"
                    (h/render [locked-input/locked-input
                               {:label-text "Label"
                                :value-text "Value"
                                :icon       "🔫"}])
                    (-> (js/expect (h/get-by-text "Label"))
                        (.toBeTruthy))
                    (-> (js/expect (h/get-by-text "Value"))
                        (.toBeTruthy))
                    (-> (js/expect (h/get-by-text "🔫"))
                        (.toBeTruthy))))
