(ns quo2.components.buttons.predictive-keyboard.component-spec
  (:require [quo2.components.buttons.predictive-keyboard.view :as predictive-keyboard]
            [test-helpers.component :as h]))

(h/describe "predictive-keyboard"
  (h/test "basic render"
    (h/render [predictive-keyboard/view {:type :error :text "Error message"}])
    (h/is-truthy (h/get-by-label-text :predictive-keyboard)))
  (h/test "rendered with correct text"
    (h/render [predictive-keyboard/view {:type :info :text "Error message"}])
    (h/is-truthy (h/get-by-text "Error message")))
  (h/test "rendered with correct words"
    (h/render [predictive-keyboard/view {:type :words :words ["lab" "label"]}])
    (h/is-truthy (h/get-by-text "lab"))
    (h/is-truthy (h/get-by-text "label")))
  (h/test "word press event"
    (let [event (h/mock-fn)]
      (h/render [predictive-keyboard/view {:type :words :words ["lab" "label"] :on-press #(event %)}])
      (h/fire-event :press (h/get-by-text "lab"))
      (h/was-called event))))
