(ns quo.components.wallet.approval-label.component-spec
  (:require [quo.components.wallet.approval-label.view :as approval-label]
            [test-helpers.component :as h]))

(h/describe "Wallet: Approval Label"
  (h/test "with :approve status"
    (h/render-with-theme-provider
     [approval-label/view
      {:status              :approve
       :customization-color :blue
       :token-value         "100"
       :token-symbol        "SNT"}])
    (h/is-truthy (h/get-by-translation-text :t/approve-amount-symbol {:amount "100" :symbol "SNT"})))

  (h/test "with :approving status"
    (h/render-with-theme-provider
     [approval-label/view
      {:status              :approving
       :customization-color :blue
       :token-value         "50"
       :token-symbol        "DAI"}])
    (h/is-truthy (h/get-by-translation-text :t/approving-amount-symbol {:amount "50" :symbol "DAI"})))

  (h/test "with :approved status"
    (h/render-with-theme-provider
     [approval-label/view
      {:status              :approved
       :customization-color :blue
       :token-value         "5"
       :token-symbol        "ETH"}])
    (h/is-truthy (h/get-by-translation-text :t/approved-amount-symbol {:amount "5" :symbol "ETH"})))

  (h/test "on-press event is called when button is pressed"
    (let [mock-fn (h/mock-fn)]
      (h/render-with-theme-provider
       [approval-label/view
        {:status              :approve
         :customization-color :blue
         :token-value         "11"
         :token-symbol        "DAI"
         :button-props        {:on-press mock-fn}}])
      (h/fire-event :press (h/get-by-translation-text :t/approve))
      (h/was-called mock-fn))))
