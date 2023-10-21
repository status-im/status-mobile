(ns quo.components.browser.new-tab.component-spec
  (:require [quo.components.browser.new-tab.view :as new-tab]
            [quo.foundations.resources :as quo.resources]
            [test-helpers.component :as h]))
(def aave (quo.resources/get-dapp :aave))
(h/describe "new-tab"
  (h/test "render with light theme and blue customization color"
    (let [theme :light
          props {:customization-color :blue}]
      (h/render-with-theme-provider [new-tab/view props] theme)
      (h/has-style (h/query-by-label-text :new-tab)
                   {:backgroundColor "rgba(42,74,245,0.1)"
                    :borderColor     "rgba(42,74,245,0.4)"
                   })
      (-> (h/expect (h/query-by-translation-text "New tab"))
          (h/is-truthy))))
  (h/test "render with dark theme and orange customization color"
    (let [theme :dark
          props {:customization-color :orange}]
      (h/render-with-theme-provider [new-tab/view props] theme)
      (h/has-style (h/query-by-label-text :new-tab)
                   {:backgroundColor "rgba(255,125,70,0.1)"
                    :borderColor     "rgba(255,125,70,0.4)"
                   })
      (-> (h/expect (h/query-by-translation-text "New tab"))
          (h/is-truthy)))))
