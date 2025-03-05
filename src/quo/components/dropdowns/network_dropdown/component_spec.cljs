(ns quo.components.dropdowns.network-dropdown.component-spec
  (:require
    [quo.components.dropdowns.network-dropdown.view :as network-dropdown]
    [quo.foundations.resources :as quo.resources]
    [test-helpers.component :as h]))

(def ^:private networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(h/describe "Network dropdown"
  (h/test "default render"
    (h/render [network-dropdown/view {:state :default}
               networks-list])
    (h/is-truthy (h/query-by-label-text :network-dropdown)))

  (h/test "Should call :on-press in enabled state"
    (let [on-press (h/mock-fn)]
      (h/render [network-dropdown/view
                 {:state    :default
                  :on-press on-press}
                 networks-list])
      (h/is-truthy (h/query-by-label-text :network-dropdown))
      (h/fire-event :press (h/query-by-label-text :network-dropdown))
      (h/was-called on-press)))

  (h/test "Should not call :on-press in disabled state"
    (let [on-press (h/mock-fn)]
      (h/render [network-dropdown/view
                 {:state    :disabled
                  :on-press on-press}
                 networks-list])
      (h/is-truthy (h/query-by-label-text :network-dropdown))
      (h/fire-event :press (h/query-by-label-text :network-dropdown))
      (h/was-not-called on-press)))

  (h/test "Should display new chain indicator"
    (h/render [network-dropdown/view
               {:state                     :default
                :show-new-chain-indicator? true}
               networks-list])
    (h/is-truthy (h/query-by-label-text :network-dropdown))
    (h/is-truthy (h/query-by-label-text :new-chain-indicator))))
