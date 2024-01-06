(ns status-im.contexts.wallet.send.transaction-progress.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.resources :as resources]
    [status-im.contexts.wallet.send.transaction-progress.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn titles
  [status]
  (case status
    :sending   (i18n/label :t/sending-with-elipsis)
    :confirmed (i18n/label :t/transaction-confirmed)
    :finalised (i18n/label :t/transacation-finalised)
    ""))

(defn view
  []
  (let [current-address (rf/sub [:wallet/current-viewing-account-address])
        leave-page      (fn []
                          (rf/dispatch [:navigate-to :wallet-accounts current-address]))
        status          (reagent/atom :sending)
        {:keys [color]} (rf/sub [:wallet/current-viewing-account])]
    [floating-button-page/view
     {:header              [quo/page-nav
                            {:type                :no-title
                             :background          :blur
                             :icon-name           :i/close
                             :margin-top          (safe-area/get-top)
                             :on-press            leave-page
                             :accessibility-label :top-bar}]
      :footer              [quo/button
                            {:customization-color color
                             :on-press            leave-page}
                            (i18n/label :t/done)]
      :customization-color color
      :gradient-cover?     true}
     [rn/view {:style style/content-container}
      [rn/image
       {:source (resources/get-image :transaction-progress)
        :style  {:margin-bottom 12}}]
      [quo/standard-title
       {:title (titles @status)}]]]))
