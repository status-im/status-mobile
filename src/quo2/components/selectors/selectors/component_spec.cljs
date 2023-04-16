(ns quo2.components.selectors.selectors.component-spec
  (:require [quo2.components.selectors.selectors.view :as selectors]
            [reagent.core :as reagent]
            [test-helpers.component :as h]))

(defn render-toggle
  ([]
   (render-toggle {}))
  ([opts]
   (h/render (reagent/as-element [selectors/toggle opts]))))

(defn render-checkbox
  ([]
   (render-checkbox {}))
  ([opts]
   (h/render (reagent/as-element [selectors/checkbox opts]))))

(defn render-checkbox-prefill
  ([]
   (render-checkbox-prefill {}))
  ([opts]
   (h/render (reagent/as-element [selectors/checkbox-prefill opts]))))

(defn render-radio
  ([]
   (render-radio {}))
  ([opts]
   (h/render (reagent/as-element [selectors/radio opts]))))

(h/test "default render of toggle component"
  (render-toggle)
  (h/is-truthy (h/get-by-test-id "toggle-component")))

(h/test "toggle component on change is working"
  (let [mock-fn (h/mock-fn)]
    (render-toggle {:on-change mock-fn})
    (h/fire-event :press (h/get-by-test-id "toggle-component"))
    (h/was-called mock-fn)))

(h/test "default render of radio component"
  (render-radio)
  (h/is-truthy (h/get-by-test-id "radio-component")))

(h/test "radio component on change is working"
  (let [mock-fn (h/mock-fn)]
    (render-radio {:on-change mock-fn})
    (h/fire-event :press (h/get-by-test-id "radio-component"))
    (h/was-called mock-fn)))

(h/test "default render of checkbox component"
  (render-checkbox)
  (h/is-truthy (h/get-by-test-id "checkbox-component")))

(h/test "checkbox component on change is working"
  (let [mock-fn (h/mock-fn)]
    (render-checkbox {:on-change mock-fn})
    (h/fire-event :press (h/get-by-test-id "checkbox-component"))
    (h/was-called mock-fn)))

(h/test "default render of checkbox-prefill component"
  (render-checkbox-prefill)
  (h/is-truthy (h/get-by-test-id "checkbox-prefill-component")))

(h/test "checkbox-prefill component on change is working"
  (let [mock-fn (h/mock-fn)]
    (render-checkbox-prefill {:on-change mock-fn})
    (h/fire-event :press (h/get-by-test-id "checkbox-prefill-component"))
    (h/was-called mock-fn)))
