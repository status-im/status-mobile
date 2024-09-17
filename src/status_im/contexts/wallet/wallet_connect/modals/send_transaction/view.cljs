(ns status-im.contexts.wallet.wallet-connect.modals.send-transaction.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.safe-area :as safe-area]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.modals.common.data-block.view :as data-block]
            [status-im.contexts.wallet.wallet-connect.modals.common.fees-data-item.view :as
             fees-data-item]
            [status-im.contexts.wallet.wallet-connect.modals.common.footer.view :as footer]
            [status-im.contexts.wallet.wallet-connect.modals.common.header.view :as header]
            [status-im.contexts.wallet.wallet-connect.modals.common.page-nav.view :as page-nav]
            [status-im.contexts.wallet.wallet-connect.modals.common.style :as style]
            [status-im.contexts.wallet.wallet-connect.utils.data-store :as data-store]
            [status-im.contexts.wallet.wallet-connect.utils.transactions :as transaction-utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- refetch-transaction
  []
  (rf/dispatch [:wallet-connect/process-eth-send-transaction true]))

(def tabs-data
  [{:id :tab/data :label (i18n/label :t/data)}
   {:id :tab/hex :label (i18n/label :t/hex)}])

(defn- render-item
  [props]
  (let [[label value] props]
    [quo/data-item
     {:card?           false
      :container-style style/data-item
      :title           (str (name label) ":")
      :subtitle        value}]))

(defn- tab-view
  [selected-tab]
  (let [{:keys [transaction]} (rf/sub [:wallet-connect/current-request])
        transaction-items     (rn/use-memo #(-> transaction
                                                (transaction-utils/transaction-hex-values->number)
                                                (data-store/data-item->array))
                                           [transaction])]
    [:<>
     (case selected-tab
       :tab/data [gesture/flat-list
                  {:data                            transaction-items
                   :content-container-style         style/data-item-container
                   :render-fn                       render-item
                   :shows-vertical-scroll-indicator false}]
       :tab/hex  [data-block/view])]))

(defn view
  []
  (let [bottom                               (safe-area/get-bottom)
        {:keys [customization-color]
         :as   account}                      (rf/sub [:wallet-connect/current-request-account-details])
        dapp                                 (rf/sub [:wallet-connect/current-request-dapp])
        network                              (rf/sub [:wallet-connect/current-request-network])
        {:keys [max-fees-fiat-formatted
                error-state estimated-time]} (rf/sub
                                              [:wallet-connect/current-request-transaction-information])
        [selected-tab set-selected-tab]      (rn/use-state (:id (first tabs-data)))
        on-change-tab                        #(set-selected-tab %)
        refetch-interval-ref                 (rn/use-ref nil)
        clear-interval                       (rn/use-callback (fn []
                                                                (when (.-current refetch-interval-ref)
                                                                  (js/clearInterval
                                                                   (.-current refetch-interval-ref))))
                                                              [refetch-interval-ref])]
    (rn/use-mount
     (fn []
       (clear-interval)
       (set! (.-current refetch-interval-ref)
         (js/setInterval refetch-transaction constants/wallet-connect-transaction-refresh-interval-ms))))

    (rn/use-unmount (fn []
                      (clear-interval)
                      (rf/dispatch [:wallet-connect/on-request-modal-dismissed])))

    [rn/view {:style (style/container bottom)}
     [quo/gradient-cover {:customization-color customization-color}]
     [page-nav/view
      {:accessibility-label :wallet-connect-sign-message-close}]
     [rn/view {:flex 1}
      [rn/view {:style style/data-content-container}
       [header/view
        {:label   (i18n/label :t/wallet-connect-send-transaction-header)
         :dapp    dapp
         :account account}]
       [quo/segmented-control
        {:size           32
         :blur?          false
         :default-active :tab/data
         :data           tabs-data
         :on-change      on-change-tab}]
       [tab-view selected-tab]]
      [footer/view
       {:warning-label     (i18n/label :t/wallet-connect-sign-warning)
        :slide-button-text (i18n/label :t/slide-to-send)
        :error-state       error-state}
       [quo/data-item
        {:status          :default
         :card?           false
         :container-style style/data-item
         :title           (i18n/label :t/network)
         :subtitle-type   :network
         :network-image   (:source network)
         :subtitle        (:full-name network)}]
       [fees-data-item/view
        {:fees       max-fees-fiat-formatted
         :fees-error error-state}]
       [quo/data-item
        {:card?           false
         :container-style style/data-item
         :title           (i18n/label :t/est-time)
         :subtitle        (if estimated-time
                            (i18n/label :t/time-in-mins {:minutes estimated-time})
                            (i18n/label :t/unknown))}]]]]))

