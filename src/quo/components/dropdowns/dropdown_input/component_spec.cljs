(ns quo.components.dropdowns.dropdown-input.component-spec
  (:require
    [quo.components.dropdowns.dropdown-input.view :as dropdown-input]
    [test-helpers.component :as h]))

(h/describe "Dropdown Input"

  (h/test "default render"
    (let [header-label "Label"
          label        "Dropdown"]
      (h/render [dropdown-input/view
                 {:label? true
                  :header-label
                  header-label} label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-truthy (h/get-by-text header-label))
      (h/is-truthy (h/get-by-text label))))

  (h/test "render with icon"
    (let [label "Dropdown"]
      (h/render [dropdown-input/view
                 {:icon? true
                  :icon-name
                  :i/wallet
                  :label? true}
                 label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-truthy (h/get-by-label-text :left-icon))
      (h/is-truthy (h/get-by-text label))))

  (h/test "render in active state"
    (let [label "Dropdown"]
      (h/render [dropdown-input/view
                 {:state :active
                  :label?
                  true} label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-truthy (h/get-by-label-text :right-icon "Expecting active state icon"))))

  (h/test "render in disabled state"
    (let [label "Dropdown"]
      (h/render [dropdown-input/view
                 {:state  :disabled
                  :label? true} label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-disabled (h/get-by-text label "Dropdown should be disabled in disabled state"))))

  (h/test "on-press"
    (let [event (h/mock-fn)
          label "Dropdown"]
      (h/render [dropdown-input/view
                 {:on-press event
                  :label?
                  true} label])
      (h/fire-event :press (h/get-by-text label))
      (h/was-called event))))
