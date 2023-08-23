(ns quo2.components.inputs.title-input.component-spec
  (:require [quo2.components.inputs.title-input.view :as title-input]
            [test-helpers.component :as h]))

(h/describe "profile: title-input"
  (h/test "renders empty"
    (h/render [title-input/title-input
               {:value          ""
                :on-change-text (h/mock-fn)}])
    (-> (js/expect (h/get-by-label-text :profile-title-input))
        (.toBeTruthy)))

  (h/test "empty text renders with max length digits and 00"
    (h/render [title-input/title-input
               {:value          ""
                :max-length     24
                :on-change-text (h/mock-fn)}])
    (-> (js/expect (h/get-by-text "00"))
        (.toBeTruthy))
    (-> (js/expect (h/get-by-text "/24"))
        (.toBeTruthy)))

  (h/test "renders with max length digits and character count"
    (h/render [title-input/title-input
               {:default-value  "abc"
                :max-length     24
                :on-change-text (h/mock-fn)} "abc"])
    (-> (js/expect (h/get-by-text "03"))
        (.toBeTruthy))
    (-> (js/expect (h/get-by-text "/24"))
        (.toBeTruthy)))

  (h/test "text updates on change"
    (let [on-change-text (h/mock-fn)]
      (h/render [title-input/title-input
                 {:value          "mock text"
                  :on-change-text on-change-text}])
      (h/fire-event :change-text (h/get-by-label-text :profile-title-input) "mock-text-new")
      (-> (js/expect on-change-text)
          (.toHaveBeenCalled)))))
