(ns quo2.components.browser.browser-input.component-spec
  (:require [quo2.components.browser.browser-input.view :as browser-input]
            [test-helpers.component :as h]))

(h/describe "Browser input"
  (h/test "Renders empty in default state"
    (h/render [browser-input/browser-input
               {:on-change-text (h/mock-fn)
                :value          ""}])
    (h/is-truthy (h/get-by-label-text :browser-input)))

  (h/test "On change text is fired"
    (let [on-change-text (h/mock-fn)]
      (h/render [browser-input/browser-input
                 {:on-change-text on-change-text
                  :value          "mock text"}])
      (h/fire-event :change-text (h/get-by-label-text :browser-input) "mock-text-new")
      (h/was-called on-change-text)))

  (h/describe "Input Label"
    (h/test "Doesn't render when not specified"
      (h/render [browser-input/browser-input])
      (h/is-null (h/query-by-label-text :browser-input-label)))

    (h/test "Renders label text when specified"
      (h/render [browser-input/browser-input
                 {:label "mock-label"}])
      (h/is-truthy (h/query-by-label-text :browser-input-label)))

    (h/test "Renders label favicon when specified alongside label text"
      (h/render [browser-input/browser-input
                 {:label "mock-label" :favicon :i/verified}])
      (h/is-truthy (h/query-by-label-text :browser-input-favicon)))

    (h/test "Renders lock icon when using ssl alongside label text"
      (h/render [browser-input/browser-input
                 {:label "mock-label" :use-ssl? true}])
      (h/is-truthy (h/query-by-label-text :browser-input-locked-icon))))

  (h/describe "Clear button"
    (h/test "Doesn't render in default state"
      (h/render [browser-input/browser-input])
      (h/is-null (h/query-by-label-text :browser-input-clear-button)))

    (h/test "Renders when there is a value"
      (h/render [browser-input/browser-input
                 {:default-value "mock text"}])
      (h/is-truthy (h/query-by-label-text :browser-input-clear-button)))

    (h/test "Is pressable"
      (let [on-clear (h/mock-fn)]
        (h/render [browser-input/browser-input
                   {:default-value "mock text"
                    :on-clear      on-clear}])
        (h/is-truthy (h/query-by-label-text :browser-input-clear-button))
        (h/fire-event :press (h/query-by-label-text :browser-input-clear-button))
        (h/was-called on-clear)))))
