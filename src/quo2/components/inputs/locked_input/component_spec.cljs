(ns quo2.components.inputs.locked-input.component-spec
  (:require [quo2.components.inputs.locked-input.view :as locked-input]
            [test-helpers.component :as h]))

(h/describe "Locked Input"
  (h/test "renders label, value and icon"
    (h/render [locked-input/locked-input
               {:label "Label"
                :icon  :i/gas} "Value"])
    (h/is-truthy (h/query-by-text "Label"))
    (h/is-truthy (h/get-by-text "Value")))

  (h/test "no value"
    (h/render [locked-input/locked-input
               {:label "Label"
                :icon  :i/gas}])
    (h/is-null (h/query-by-text "Value"))
    (h/is-truthy (h/get-by-text "Label")))

  (h/test "no emoji"
    (h/render [locked-input/locked-input
               {:label "Label"} "Value"])
    (h/is-truthy (h/get-by-text "Label"))
    (h/is-truthy (h/get-by-text "Value"))))
