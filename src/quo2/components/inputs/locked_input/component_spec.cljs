(ns quo2.components.inputs.locked-input.component-spec
  (:require [quo2.components.inputs.locked-input.view :as locked-input]
            [test-helpers.component :as h]))

(h/describe "Locked Input"
            (h/test "renders label, value and icon"
                    (h/render [locked-input/locked-input
                               {:label-text "Label"
                                :value-text "Value"
                                :icon       "🔫"}])
                    (h/is-truthy (h/get-by-text "Label"))
                    (h/is-truthy (h/get-by-text "Value"))
                    (h/is-truthy (h/get-by-text "🔫")))

            (h/test "no value"
                    (h/render [locked-input/locked-input
                               {:label-text "Label"
                                :icon       "🔫"}])
                    (h/is-falsy (h/get-by-text "Value"))
                    (h/is-truthy (h/get-by-text "Label"))
                    (h/is-truthy (h/get-by-text "🔫")))

            (h/test "no emoji"
                    (h/render [locked-input/locked-input
                               {:label-text "Label"
                                :value-text "Value"}])
                    (h/is-falsy (h/get-by-text "🔫"))
                    (h/is-truthy (h/get-by-text "Label"))
                    (h/is-truthy (h/get-by-text "Value"))))
