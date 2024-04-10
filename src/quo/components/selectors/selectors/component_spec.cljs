(ns quo.components.selectors.selectors.component-spec
  (:require
    [quo.components.selectors.selectors.view :as selectors]
    [reagent.core :as reagent]
    [test-helpers.component :as h]))

(defn render-toggle
  ([]
   (render-toggle {:type :toggle}))
  ([opts]
   (h/render (reagent/as-element [selectors/view (assoc opts :type :toggle)]))))

(defn render-checkbox
  ([]
   (render-checkbox {:type :checkbox}))
  ([opts]
   (h/render (reagent/as-element [selectors/view (assoc opts :type :checkbox)]))))

(defn render-filled-checkbox
  ([]
   (render-filled-checkbox {:type :filled-checkbox}))
  ([opts]
   (h/render (reagent/as-element [selectors/view (assoc opts :type :filled-checkbox)]))))

(defn render-radio
  ([]
   (render-radio {:type :radio}))
  ([opts]
   (h/render (reagent/as-element [selectors/view (assoc opts :type :radio)]))))

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

(h/test "default render of filled-checkbox component"
  (render-filled-checkbox)
  (h/is-truthy (h/get-by-test-id "filled-checkbox-component")))

(h/test "filled-checkbox component on change is working"
  (let [mock-fn (h/mock-fn)]
    (render-filled-checkbox {:on-change mock-fn})
    (h/fire-event :press (h/get-by-test-id "filled-checkbox-component"))
    (h/was-called mock-fn)))
