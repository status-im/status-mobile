(ns quo2.components.browser.browser-input.component-spec
  (:require [quo2.components.browser.browser-input.view :as browser-input]
            [test-helpers.component :as h]))

(h/describe "Browser input"
  (h/test "Renders empty in default state"
    (h/render [browser-input/view
               {:on-change-text (h/mock-fn)
                :value          ""}])
    (h/is-truthy (h/get-by-label-text :browser-input)))

  (h/test "On change text is fired"
    (let [on-change-text (h/mock-fn)]
      (h/render [browser-input/view
                 {:on-change-text on-change-text
                  :value          "mock text"}])
      (h/fire-event :change-text (h/get-by-label-text :browser-input) "mock-text-new")
      (h/was-called on-change-text)))

  (h/describe "Input Label"
    (h/test "Doesn't render label text when input is focused"
      (h/render [browser-input/view
                 {:auto-focus true}])
      (h/is-null (h/query-by-label-text :browser-input-label)))

    (h/test "Renders label text when input has a value and input is not focused"
      (h/render [browser-input/view
                 {:default-value "mock default"}])
      (h/is-truthy (h/query-by-label-text :browser-input-label)))

    (h/test "Renders site favicon when specified"
      (h/render [browser-input/view
                 {:default-value "mock default" :favicon :i/verified}])
      (h/is-truthy (h/query-by-label-text :browser-input-favicon)))

    (h/test "Renders lock icon when lock is enabled"
      (h/render [browser-input/view
                 {:default-value "mock default" :locked? true}])
      (h/is-truthy (h/query-by-label-text :browser-input-locked-icon))))

  (h/describe "Clear button"
    (h/test "Doesn't render in default state"
      (h/render [browser-input/view])
      (h/is-null (h/query-by-label-text :browser-input-clear-button)))

    (h/test "Renders when there is a value"
      (h/render [browser-input/view
                 {:default-value "mock text"}])
      (h/is-truthy (h/query-by-label-text :browser-input-clear-button)))

    (h/test "Is pressable"
      (let [on-clear (h/mock-fn)]
        (h/render [browser-input/view
                   {:default-value "mock text"
                    :on-clear      on-clear}])
        (h/is-truthy (h/query-by-label-text :browser-input-clear-button))
        (h/fire-event :press (h/query-by-label-text :browser-input-clear-button))
        (h/was-called on-clear)))))
