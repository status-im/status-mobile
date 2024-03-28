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
      (render [amount-input/view {:value text-expected}])
      (h/is-truthy (h/query-by-label-text :amount-input))))

  (h/test "When the value = minimum dec button is disabled"
    (render [amount-input/view
             {:value     0
              :min-value 0}])
    (h/is-truthy
     (oops/oget (h/get-by-label-text :amount-input-dec-button) "props" "accessibilityState" "disabled")))

  (h/test "When the value = maximum inc button is disabled"
    (render [amount-input/view
             {:value     100
              :max-value 100}])
    (h/is-truthy
     (oops/oget (h/get-by-label-text :amount-input-inc-button) "props" "accessibilityState" "disabled")))

  (h/test "Renders the error state"
    (render [amount-input/view {:status :error :value 10}])
    (h/is-equal (colors/resolve-color :danger :light)
                (oops/oget (h/get-by-label-text :amount-input) "props" "style" "color"))))
