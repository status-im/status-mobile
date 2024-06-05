(ns status-im.contexts.wallet.send.transaction-progress.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.send.transaction-progress.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn titles
  [status]
  (case status
    :pending   (i18n/label :t/sending-with-ellipsis)
    :confirmed (i18n/label :t/transaction-confirmed)
    :finalised (i18n/label :t/transacation-finalised)
    ""))

(defn combined-status-overview
  [transaction-details]
  (cond
    (every? (fn [[_k v]] (= (:status v) :finalised)) transaction-details) :finalised
    (some (fn [[_k v]] (= (:status v) :pending)) transaction-details)     :pending
    (some (fn [[_k v]] (= (:status v) :confirmed)) transaction-details)   :confirmed
    :else                                                                 nil))

(defn- footer
  [{:keys [color leave-page]}]
  (let [send-to-address      (rf/sub [:wallet/wallet-send-to-address])
        save-address-hidden? (rf/sub [:wallet/address-saved? send-to-address])]
    [quo/bottom-actions
     {:actions          (if save-address-hidden? :one-action :two-actions)
      :button-two-label (i18n/label :t/save-address)
      :button-two-props {:type                :grey
                         :icon-left           :i/contact-book
                         :accessibility-label :save-address
                         :on-press            (rn/use-callback
                                               #(rf/dispatch [:open-modal
                                                              :screen/settings.save-address
                                                              {:address send-to-address}]))}
      :button-one-label (i18n/label :t/done)
      :button-one-props {:customization-color color
                         :type                :primary
                         :accessibility-label :done
                         :on-press            leave-page}}]))

(defn view
  []
  (let [leave-page      #(rf/dispatch [:wallet/end-transaction-flow])
        {:keys [color]} (rf/sub [:wallet/current-viewing-account])]
    (fn []
      (rn/use-effect
       (fn []
         (rf/dispatch [:wallet/get-saved-addresses])))
      (let [transaction-details (rf/sub [:wallet/send-transaction-progress])]
        [floating-button-page/view
         {:footer-container-padding 0
          :header                   [quo/page-nav
                                     {:type                :no-title
                                      :background          :blur
                                      :icon-name           :i/close
                                      :margin-top          (safe-area/get-top)
                                      :on-press            leave-page
                                      :accessibility-label :top-bar}]
          :footer                   [footer
                                     {:color      color
                                      :leave-page leave-page}]
          :customization-color      color
          :gradient-cover?          true}
         [rn/view {:style style/content-container}
          [rn/image
           {:source (resources/get-image :transaction-progress)
            :style  {:margin-bottom 12}}]
          [quo/standard-title
           {:container-style {:flex 1}
            :title           (titles (combined-status-overview transaction-details))}]]]))))
