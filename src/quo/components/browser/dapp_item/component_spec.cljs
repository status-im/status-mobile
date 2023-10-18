(ns quo.components.browser.dapp-item.component-spec
  (:require [quo.components.browser.dapp-item.view :as dapp-item]
            [quo.foundations.resources :as quo.resources]
            [test-helpers.component :as h]))
(def aave (quo.resources/get-dapp :aave))
(h/describe "dapp-item"
  (h/test "default render"
    (let [theme :light
          props {:logo aave
                 :name "Aave"}]
      (h/render-with-theme-provider [dapp-item/view props] theme)
      (h/is-truthy (h/query-by-text "Aave")))))
