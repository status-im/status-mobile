(ns status-im.contexts.shell.share.wallet.component-spec
  (:require
    [status-im.contexts.shell.share.wallet.view :as wallet-view]
    status-im.contexts.wallet.events
    [test-helpers.component :as h]))

(defn render-wallet-view
  []
  (let [component-rendered (h/render [wallet-view/wallet-tab])
        rerender-fn        (h/get-rerender-fn component-rendered)
        share-qr-code      (h/get-by-label-text :share-qr-code)]
    ;; Fires on-layout since it's needed to render the content
    (h/fire-event :layout share-qr-code #js {:nativeEvent #js {:layout #js {:width 500}}})
    (rerender-fn [wallet-view/wallet-tab])))

(h/describe "share wallet accounts"
  (h/setup-restorable-re-frame)
  (h/before-each
   (fn []
     (h/setup-subs {:dimensions/window-width 500
                    :mediaserver/port        200
                    :wallet/accounts         [{:address "0x707f635951193ddafbb40971a0fcaab8a6415160"
                                               :name    "Wallet One"
                                               :emoji   "😆"
                                               :color   :blue}]})))
  (h/test "should display the the wallet tab"
    (render-wallet-view)
    (h/wait-for #(h/is-truthy (h/get-by-text "Wallet One"))))

  (h/test "should display the the legacy account"
    (render-wallet-view)
    (-> (h/wait-for #(h/get-by-label-text :share-qr-code-legacy-tab))
        (.then (fn []
                 (h/fire-event :press (h/get-by-label-text :share-qr-code-legacy-tab))
                 (-> (h/wait-for #(h/is-falsy (h/query-by-text "eth:"))))))))

  (h/test "should display the the multichain account"
    (render-wallet-view)
    (-> (h/wait-for #(h/get-by-label-text :share-qr-code-multichain-tab))
        (.then (fn []
                 (h/fire-event :press (h/get-by-label-text :share-qr-code-multichain-tab))
                 (-> (h/wait-for #(h/is-truthy (h/query-by-text "eth:"))))))))

)

