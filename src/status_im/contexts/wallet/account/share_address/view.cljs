(ns status-im.contexts.wallet.account.share-address.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.account.share-address.style :as style]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.sheets.network-preferences.view :as network-preferences]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def qr-size 500)

(defn- share-action
  [address share-title]
  (rf/dispatch [:open-share
                {:options (if platform/ios?
                            {:activityItemSources [{:placeholderItem {:type    :text
                                                                      :content address}
                                                    :item            {:default {:type :text
                                                                                :content
                                                                                address}}
                                                    :linkMetadata    {:title share-title}}]}
                            {:title     share-title
                             :subject   share-title
                             :message   address
                             :isNewTask true})}]))

(defn- open-preferences
  [selected-networks]
  (let [on-save       (fn [chain-ids]
                        (rf/dispatch [:hide-bottom-sheet])
                        (reset! selected-networks (map utils/id->network chain-ids)))
        sheet-content (fn []
                        [network-preferences/view
                         {:blur?             true
                          :selected-networks (set @selected-networks)
                          :on-save           on-save
                          :button-label      (i18n/label :t/display)}])]
    (rf/dispatch [:show-bottom-sheet
                  {:theme   :dark
                   :shell?  true
                   :content sheet-content}])))


(defn view
  []
  (let [padding-top         (:top (safe-area/get-insets))
        wallet-type         (reagent/atom :legacy)
        ;; Design team is yet to confirm the default selected networks here. Should be the current
        ;; selected for the account or all the networks always
        selected-networks   (reagent/atom constants/default-network-names)
        on-settings-press   #(open-preferences selected-networks)
        on-legacy-press     #(reset! wallet-type :legacy)
        on-multichain-press #(reset! wallet-type :multichain)]
    (fn []
      (let [{:keys [address color emoji watch-only?]
             :as   account}     (rf/sub [:wallet/current-viewing-account])
            share-title         (str (:name account) " " (i18n/label :t/address))
            qr-url              (utils/get-wallet-qr {:wallet-type @wallet-type
                                                      :selected-networks
                                                      @selected-networks
                                                      :address address})
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         qr-url
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     qr-size
                                  :error-level :highest})
            {:keys [status]}    (rf/sub [:get-screen-params])
            title               (case status
                                  :share   (i18n/label :t/share-address)
                                  :receive (i18n/label :t/receive)
                                  nil)]
        [quo/overlay {:type :shell}
         [rn/view
          {:flex        1
           :padding-top padding-top}
          [quo/page-nav
           {:icon-name           :i/close
            :on-press            #(rf/dispatch [:navigate-back])
            :background          :blur
            :right-side          [{:icon-name :i/scan
                                   :on-press  (fn []
                                                (rf/dispatch [:navigate-back])
                                                (rf/dispatch [:open-modal :shell-qr-reader]))}]
            :accessibility-label :top-bar}]
          [quo/page-top
           {:title           title
            :container-style style/header-container}]
          [rn/view {:style {:padding-horizontal 20}}
           [quo/share-qr-code
            {:type                (if watch-only? :watched-address :wallet)
             :address             @wallet-type
             :qr-image-uri        qr-media-server-uri
             :qr-data             qr-url
             :networks            @selected-networks
             :on-share-press      #(share-action qr-url share-title)
             :profile-picture     nil
             :full-name           (:name account)
             :customization-color color
             :emoji               emoji
             :on-legacy-press     on-legacy-press
             :on-multichain-press on-multichain-press
             :on-settings-press   on-settings-press}]]]]))))
