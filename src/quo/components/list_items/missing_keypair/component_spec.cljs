(ns quo.components.list-items.missing-keypair.component-spec
  (:require
    [quo.components.list-items.missing-keypair.view :as missing-keypair]
    [test-helpers.component :as h]))

(def keypair-data
  {:accounts []
   :name     "Key Pair Name"})

(def props
  {:keypair          keypair-data
   :blur?            true
   :on-options-press (fn [])})

(h/describe "List items: missing keypair item"
  (h/test "Test item container renders"
    (h/render-with-theme-provider [missing-keypair/view props])
    (h/is-truthy (h/get-by-label-text :missing-keypair-item)))
  (h/test "Test keypair icon renders"
    (h/render-with-theme-provider [missing-keypair/view props])
    (h/is-truthy (h/get-by-label-text :icon)))
  (h/test "Test name renders"
    (h/render-with-theme-provider [missing-keypair/view props])
    (h/is-truthy (h/get-by-label-text :name)))
  (h/test "Test preview-list renders"
    (h/render-with-theme-provider [missing-keypair/view props])
    (h/is-truthy (h/get-by-label-text :preview-list)))
  (h/test "Test options button renders"
    (h/render-with-theme-provider [missing-keypair/view props])
    (h/is-truthy (h/get-by-label-text :options-button))))
