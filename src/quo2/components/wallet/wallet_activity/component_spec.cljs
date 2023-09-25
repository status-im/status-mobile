(ns quo2.components.wallet.wallet-activity.component-spec
  (:require [quo2.components.wallet.wallet-activity.view :as wallet-activity]
            [test-helpers.component :as h]))

(h/describe "Wallet activity"
  (h/test "default render"
    (h/render [wallet-activity/view {}])
    (h/is-truthy (h/query-by-label-text :wallet-activity)))

  (h/test "Should call :on-press"
    (let [on-press (h/mock-fn)]
      (h/render [wallet-activity/view {:on-press on-press}])
      (h/is-truthy (h/query-by-label-text :wallet-activity))
      (h/fire-event :press (h/query-by-label-text :wallet-activity))
      (h/was-called on-press))))
