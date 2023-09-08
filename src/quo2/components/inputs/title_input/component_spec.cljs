(ns quo2.components.inputs.title-input.component-spec
  (:require [quo2.components.inputs.title-input.view :as title-input]
            [test-helpers.component :as h]))

(h/describe "input -> title-input component"
  (h/test "renders empty"
    (h/render [title-input/view {:value ""}])
    (h/is-truthy (h/query-by-label-text :profile-title-input)))

  (h/test "empty text renders with max length digits and 00"
    (h/render [title-input/view
               {:value      ""
                :max-length 24}])
    (h/fire-event :on-focus (h/query-by-label-text :profile-title-input))
    (h/wait-for #(h/is-truthy (h/query-by-text "00")))
    (h/is-truthy (h/query-by-text "/24")))

  (h/test "renders with max length digits and character count"
    (h/render [title-input/view
               {:default-value "abc"
                :max-length    24} "abc"])
    (h/fire-event :on-focus (h/query-by-label-text :profile-title-input))
    (h/wait-for #(h/is-truthy (h/query-by-text "03")))
    (h/is-truthy (h/query-by-text "/24")))

  (h/test "text updates on change"
    (let [on-change-text (h/mock-fn)]
      (h/render [title-input/view
                 {:value          "mock text"
                  :on-change-text on-change-text}])
      (h/fire-event :change-text (h/get-by-label-text :profile-title-input) "mock-text-new")
      (h/was-called on-change-text))))
