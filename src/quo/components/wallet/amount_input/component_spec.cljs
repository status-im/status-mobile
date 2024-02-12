(ns quo.components.wallet.amount-input.component-spec
  (:require
    [oops.core :as oops]
    [quo.components.wallet.amount-input.view :as amount-input]
    [quo.foundations.colors :as colors]
    [test-helpers.component :as h]))

(defn- render
  [component]
  (h/render-with-theme-provider component :light))

(h/describe "Amount input component"
  (h/test "Renders with default value"
    (let [text-expected 0]
      (render [amount-input/view {:init-value text-expected}])
      (h/is-truthy (h/query-by-label-text :amount-input))
      (h/is-equal (oops/oget (h/get-by-label-text :amount-input) "props" "value")
                  (str text-expected))))

  (h/test "When the value = minimum dec button is disabled"
    (render [amount-input/view
             {:init-value 0
              :min-value  0}])
    (h/is-truthy
     (oops/oget (h/get-by-label-text :amount-input-dec-button) "props" "accessibilityState" "disabled")))

  (h/test "When the value = maximum inc button is disabled"
    (render [amount-input/view
             {:init-value 100
              :max-value  100}])
    (h/is-truthy
     (oops/oget (h/get-by-label-text :amount-input-inc-button) "props" "accessibilityState" "disabled")))

  (h/test "Renders the error state"
    (render [amount-input/view {:status :error}])
    (h/is-equal (colors/resolve-color :danger :light)
                (oops/oget (h/get-by-label-text :amount-input) "props" "style" "color")))

  (h/test "on-change-text function is fired"
    (let [on-change-text (h/mock-fn)]
      (render [amount-input/view {:on-change-text on-change-text}])
      (h/fire-event :change-text (h/get-by-label-text :amount-input) "100")
      (h/was-called on-change-text))))
