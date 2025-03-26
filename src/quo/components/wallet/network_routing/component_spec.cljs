(ns quo.components.wallet.network-routing.component-spec
  (:require [oops.core :as oops]
            [quo.components.wallet.network-routing.view :as network-routing]
            [quo.theme]
            [reagent.core :as reagent]
            [test-helpers.component :as h]))

(h/describe "Network-routing tests"
  (let [network       {:amount 250 :max-amount 300 :network-name :unknown}
        default-props {:networks           [network network network]
                       :total-amount       500
                       :requesting-data?   false
                       :on-amount-selected (fn [_new-amount _network-idx] nil)}]
    (h/test "Renders Default"
      (h/render-with-theme-provider [network-routing/view default-props])
      (h/is-truthy (h/get-by-label-text :network-routing)))

    (h/test "Renders bars inside"
      (let [component   (h/render-with-theme-provider [network-routing/view default-props])
            rerender-fn #((oops/oget component "rerender") (reagent/as-element %))
            component   (h/get-by-label-text :network-routing)]
        ;; Fires on-layout callback since the total width is required
        (h/fire-event :layout component #js {:nativeEvent #js {:layout #js {:width 1000}}})
        ;; Update props to trigger rerender, otherwise it won't be updated
        (rerender-fn [quo.theme/provider {:theme :light}
                      [network-routing/view (assoc default-props :requesting-data? true)]])
        ;; Check number of networks rendered
        (->> (js->clj (h/query-all-by-label-text :network-routing-bar))
             (count)
             (h/is-equal 3))))))
