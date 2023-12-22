(ns status-im.contexts.wallet.receive.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [react-native.share :as share]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.sheets.network-preferences.view :as network-preferences]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.receive.style :as style]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def qr-size 500)

(defn- share-action
  [address share-title]
  (share/open
   (if platform/ios?
     {:activity-item-sources [{:placeholder-item {:type    "text"
                                                  :content address}
                               :item             {:default {:type "text"
                                                            :content
                                                            address}}
                               :link-metadata    {:title share-title}}]}
     {:title   share-title
      :subject share-title
      :message address})))

(defn- open-preferences
  [selected-networks]
  (rf/dispatch [:show-bottom-sheet
                {:theme :dark
                 :shell? true
                 :content
                 (fn []
                   [network-preferences/view
                    {:blur?             true
                     :selected-networks (set @selected-networks)
                     :on-save           (fn [chain-ids]
                                          (rf/dispatch [:hide-bottom-sheet])
                                          (reset! selected-networks (map #(get utils/id->network %)
                                                                         chain-ids)))}])}]))


(defn view
  []
  (let [padding-top       (:top (safe-area/get-insets))
        wallet-type       (reagent/atom :wallet-legacy)
        ;; Design team is yet to confirm the default selected networks here.
        ;; Should be the current selected for the account or all the networks always
        selected-networks (reagent/atom [:ethereum :optimism :arbitrum])]
    (fn []
      (let [{:keys [address color emoji] :as account} (rf/sub [:wallet/current-viewing-account])
            share-title                               (str (:name account) " " (i18n/label :t/address))
            qr-url                                    (utils/get-wallet-qr {:wallet-type @wallet-type
                                                                            :selected-networks
                                                                            @selected-networks
                                                                            :address address})
            qr-media-server-uri                       (image-server/get-qr-image-uri-for-any-url
                                                       {:url         qr-url
                                                        :port        (rf/sub [:mediaserver/port])
                                                        :qr-size     qr-size
                                                        :error-level :highest})]
        [quo/overlay {:type :shell}
         [rn/view
          {:flex        1
           :padding-top padding-top}
          [quo/page-nav
           {:icon-name           :i/close
            :on-press            #(rf/dispatch [:navigate-back])
            :background          :blur
            :right-side          [{:icon-name :i/scan
                                   :on-press  #(js/alert "To be implemented")}]
            :accessibility-label :top-bar}]
          [quo/text-combinations
           {:container-style style/header-container
            :title           (i18n/label :t/receive)}]
          [rn/view {:style {:padding-horizontal 20}}
           [quo/share-qr-code
            {:type                @wallet-type
             :qr-image-uri        qr-media-server-uri
             :qr-data             qr-url
             :networks            @selected-networks
             :on-share-press      #(share-action qr-url share-title)
             :profile-picture     nil
             :unblur-on-android?  true
             :full-name           (:name account)
             :customization-color color
             :emoji               emoji
             :on-legacy-press     #(reset! wallet-type :wallet-legacy)
             :on-multichain-press #(reset! wallet-type :wallet-multichain)
             :on-settings-press   #(open-preferences selected-networks)}]]]]))))
