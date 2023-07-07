(ns quo2.components.buttons.button.component-spec
  (:require [quo2.components.buttons.button.view :as button]
            [test-helpers.component :as h]))

(defn render-button
  ([options label]
   (h/render [button/button options label])))

(h/describe "button tests"
  (h/test "default render of button component"
    (fn []
      (render-button {:accessibility-label "test-button"} "")
      (h/is-truthy (h/get-by-label-text "test-button"))))

  (h/test "button renders with a label"
    (fn []
      (render-button {} "test-label")
      (h/is-truthy (h/get-by-text "test-label"))))

  (h/test "button on-press works"
    (let [event (h/mock-fn)]
      (fn []
        (render-button {:on-press event} "test-label")
        (h/fire-event :press (h/get-by-text "test-label"))
        (h/was-called event)))))
