(ns status-im.contexts.wallet.share-community.view
  (:require
   [quo.core :as quo]
   [react-native.core :as rn]
   [react-native.safe-area :as safe-area]
   [reagent.core :as reagent]
   [status-im.contexts.wallet.common.utils :as utils]
   [status-im.contexts.wallet.share-community.style :as style]
   [utils.i18n :as i18n]
   [utils.image-server :as image-server]
   [utils.re-frame :as rf]))

(def qr-size 500)


(defn view
  []
  (let [padding-top       (:top (safe-area/get-insets))
        wallet-type       (reagent/atom :legacy)
        ;; Design team is yet to confirm the default selected networks here.
        ;; Should be the current selected for the account or all the networks always
        selected-networks (reagent/atom [:ethereum :optimism :arbitrum])]
    (fn []
      (let [{:keys [address color emoji watch-only?]
             :as   account}     (rf/sub [:wallet/current-viewing-account])
            qr-url              (utils/get-wallet-qr {:wallet-type @wallet-type
                                                      :selected-networks
                                                      @selected-networks
                                                      :address address})
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         qr-url
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     qr-size
                                  :error-level :highest})
            title               "Share community"]
        [quo/overlay {:type :shell}
         [rn/view
          {:flex        1
           :padding-top padding-top
           :key         :share-community}
          [quo/page-nav
           {:icon-name           :i/close
            :on-press            #(rf/dispatch [:navigate-back])
            :background          :blur
            :right-side          [{:icon-name :i/scan
                                   :on-press  #(js/alert "To be implemented")}]
            :accessibility-label :top-bar}]
          [quo/text-combinations
           {:container-style style/header-container
            :title           title}]
          [rn/view {:style {:padding-horizontal 20}}
           [quo/share-community-qr-code
            {:type                (if watch-only? :watched-address :wallet)
             :qr-image-uri        qr-media-server-uri
             :qr-data             qr-url
             :networks            @selected-networks
             :profile-picture     nil
             :full-name           (:name account)
             :customization-color color
             :emoji               emoji}]]]]))))
