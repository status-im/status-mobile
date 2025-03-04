(ns status-im.contexts.wallet.trading.swap.view
  (:require
    [oops.core :as oops]
    [react-native.core :as rn]
    [status-im.contexts.wallet.routes.hooks :as routes.hooks]
    [status-im.contexts.wallet.trading.swap.components.exchange-rate.view :as exchange-rate]
    [status-im.contexts.wallet.trading.swap.components.flip-tokens :as flip-tokens]
    [status-im.contexts.wallet.trading.swap.components.header-nav :as header-nav]
    [status-im.contexts.wallet.trading.swap.components.keyboard :as swap-keyboard]
    [status-im.contexts.wallet.trading.swap.components.pay-input :as pay-input]
    [status-im.contexts.wallet.trading.swap.components.receive-input :as receive-input]
    status-im.contexts.wallet.trading.swap.events
    [status-im.contexts.wallet.trading.swap.hooks :as hooks]
    [status-im.contexts.wallet.trading.swap.routes :as routes]
    [status-im.contexts.wallet.trading.swap.style :as style]
    [utils.re-frame :as rf]))

(defn- blur-input
  [input-ref]
  (some-> @input-ref
          (oops/ocall "blur")))

(defn view
  []
  (let [db-pay-amount                             (rf/sub [:trading.swap/pay-amount])
        {:keys [clear-input-value] :as pay-input} (hooks/use-swap-input db-pay-amount)
        input-ref                                 (rn/use-ref-atom nil)
        pay-amount                                (:value pay-input)
        pay-token                                 (rf/sub [:trading.swap/pay-token])
        pay-input-focused?                        (rf/sub [:trading.swap/focused?])
        last-approval-status                      (rf/sub [:trading.swap/last-approval-status])
        on-pay-input-focus                        (rn/use-callback
                                                   (fn []
                                                     (rf/dispatch [:trading.swap/focus-input]))
                                                   [pay-input pay-token])]
    (hooks/use-init-swap-trading)
    (hooks/use-suggested-routes pay-amount)

    (rn/use-effect
     (fn []
       (if pay-input-focused?
         (rf/dispatch [:shell/hide-tabs])
         (rf/dispatch [:shell/show-tabs]))
       (when-not pay-input-focused?
         (clear-input-value)
         (blur-input input-ref)))
     [pay-input-focused?])

    (routes.hooks/use-transactions-sent
     (fn [transactions]
       (let [approval (routes/find-pending-approval-transaction transactions)]
         (if approval
           (rf/dispatch [:trading.swap/on-approval-sent approval])
           (do (clear-input-value)
               (rf/dispatch [:trading.swap/on-swap-sent]))))))

    (hooks/use-approval-finished
     (fn []
       (rf/dispatch [:trading.swap/on-approval-finished last-approval-status])))

    (rn/use-effect
     (fn []
       (rf/dispatch [:trading.swap/set-pay-amount pay-amount]))
     [pay-amount])

    (println :focused? pay-input-focused?)
    [rn/view {:style (style/home-container)}
     [header-nav/view]
     [rn/scroll-view
      {:style style/inputs-container}
      [pay-input/view
       {:input-ref          input-ref
        :pay-input          pay-input
        :pay-input-focused? pay-input-focused?
        :on-pay-input-focus on-pay-input-focus}]
      [flip-tokens/view
       {:pay-input pay-input}]
      [receive-input/view
       {:pay-input pay-input}]
      [exchange-rate/view]]
     #_[rn/view {:style style/footer-container}
        (when-not pay-input-focused?
          [footer/view])]
     (when pay-input-focused?
       [swap-keyboard/keyboard-view
        {:input                pay-input
         :input-token-decimals (:decimals pay-token)}])]))
