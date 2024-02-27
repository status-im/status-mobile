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

(defn footer [{:keys [color leave-page]}]
  [rn/view {:style style/footer}
   [quo/button {:customization-color color
                :on-press            leave-page}
    (i18n/label :t/done)]])

(defn view
  []
  (let [leave-page      #(rf/dispatch [:wallet/close-transaction-progress-page])
        {:keys [color]} {:color "red"} ;; (rf/sub [:wallet/current-viewing-account])
        ]
    (fn []
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
          :footer                   [rn/view {:style style/footer}
                                     [quo/button {:type     :grey
                                                  :on-press leave-page}
                                      (i18n/label :t/save-address)]
                                     [quo/button {:customization-color color
                                                  :type                :primary
                                                  :on-press            leave-page}
                                      (i18n/label :t/done)]]
          :customization-color      color
          :gradient-cover?          true}
         [rn/view {:style style/content-container}
          [rn/image
           {:source (resources/get-image :transaction-progress)
            :style  {:margin-bottom 12}}]
          [quo/standard-title
           {:title (titles (combined-status-overview transaction-details))}]]]))))

(comment
  ,)
