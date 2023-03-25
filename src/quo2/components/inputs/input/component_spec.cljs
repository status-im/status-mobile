(ns quo2.components.inputs.input.component-spec
  (:require [quo2.components.inputs.input.view :as input]
            [test-helpers.component :as h]))

(h/describe "Input"
  (h/test "default render"
    (h/render [input/input])
    (h/is-truthy (h/query-by-label-text :input))
    (h/is-null (h/query-by-label-text :password-input)))

  (h/test "Password type"
    (h/render [input/input {:type :password}])
    (h/is-truthy (h/query-by-label-text :password-input))
    (h/is-null (h/query-by-label-text :input)))

  (h/describe "Icon"
    (h/test "Doesn't exist in base input"
      (h/render [input/input])
      (h/is-null (h/query-by-label-text :input-icon)))

    (h/test "Renders"
      (h/render [input/input {:icon-name :i/placeholder}])
      (h/is-truthy (h/get-by-label-text :input-icon))))

  (h/describe "Right accessory"
    (h/test "Doesn't exist in base input"
      (h/render [input/input])
      (h/is-null (h/query-by-label-text :input-right-icon)))

    (h/test "Clear icon"
      (h/render [input/input {:clearable? true}])
      (h/is-truthy (h/query-by-label-text :input-right-icon)))

    (h/test "Password icon"
      (h/render [input/input {:type :password}])
      (h/is-truthy (h/query-by-label-text :input-right-icon))))

  (h/describe "Button"
    (h/test "Doesn't exist in base input"
      (h/render [input/input])
      (h/is-null (h/query-by-label-text :input-button)))

    (h/test "Renders with given text and it's pressable"
      (let [button-text     "This is a button"
            button-callback (h/mock-fn)]
        (h/render [input/input
                   {:button {:on-press button-callback
                             :text     button-text}}])
        (h/is-truthy (h/query-by-label-text :input-button))
        (h/is-truthy (h/get-by-text button-text))
        (h/fire-event :press (h/query-by-label-text :input-button))
        (h/was-called button-callback))))

  (h/describe "Label"
    (h/test "Doesn't exist in base input"
      (h/render [input/input])
      (h/is-null (h/query-by-label-text :input-labels)))

    (h/test "Renders with specified text"
      (let [input-label "My label"]
        (h/render [input/input {:label input-label}])
        (h/is-truthy (h/query-by-label-text :input-labels))
        (h/is-truthy (h/get-by-text input-label)))))

  (h/test "Char limit counter"
    (let [char-limit     100
          char-limit-str (str "0/" char-limit)]
      (h/render [input/input {:char-limit char-limit}])
      (h/is-truthy (h/query-by-label-text :input-labels))
      (h/is-truthy (h/get-by-text char-limit-str)))))
