(ns quo2.components.dropdowns.dropdown.component-spec
  (:require
    [quo2.components.dropdowns.dropdown.view :as dropdown]
    [test-helpers.component :as h]))

(h/describe "Dropdown"
  (h/test "default render"
    (let [label "Recent"]
      (h/render [dropdown/view {} label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-null (h/query-by-label-text :left-icon))
      (h/is-truthy (h/get-by-text label))
      (h/is-truthy (h/get-by-label-text :right-icon))))

  (h/test "render with icon"
    (let [label "Recent"]
      (h/render [dropdown/view {:icon? true} label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-truthy (h/get-by-label-text :left-icon))
      (h/is-truthy (h/get-by-text label))
      (h/is-truthy (h/get-by-label-text :right-icon))))

  (h/test "render only emoji"
    (let [label "🍑"]
      (h/render [dropdown/view {:emoji? true} label])
      (h/is-truthy (h/get-by-label-text :dropdown))
      (h/is-null (h/query-by-label-text :left-icon))
      (h/is-truthy (h/get-by-text label))
      (h/is-null (h/query-by-label-text :right-icon))))

  (h/test "on-press"
    (let [event (h/mock-fn)
          label "Recent"]
      (h/render [dropdown/view {:on-press event} label])
      (h/fire-event :press (h/get-by-text label))
      (h/was-called event))))
