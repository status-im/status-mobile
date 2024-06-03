(ns status-im.contexts.settings.wallet.saved-addresses.sheets.share-address.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.sheets.network-preferences.view :as network-preferences]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def qr-size 500)

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

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
  [selected-networks set-selected-networks]
  (let [on-save       (fn [chain-ids]
                        (rf/dispatch [:hide-bottom-sheet])
                        (set-selected-networks (map network-utils/id->network chain-ids)))
        sheet-content (fn []
                        [network-preferences/view
                         {:blur?             true
                          :selected-networks (set selected-networks)
                          :on-save           on-save
                          :button-label      (i18n/label :t/display)}])]
    (rf/dispatch [:show-bottom-sheet
                  {:theme   :dark
                   :shell?  true
                   :content sheet-content}])))

(defn view
  []
  (let [padding-top                                (:top (safe-area/get-insets))
        {:keys [name address customization-color]} (rf/sub [:get-screen-params])
        [wallet-type set-wallet-type]              (rn/use-state :legacy)
        [selected-networks set-selected-networks]  (rn/use-state constants/default-network-names)
        on-settings-press                          (rn/use-callback #(open-preferences
                                                                      selected-networks
                                                                      set-selected-networks)
                                                                    [selected-networks])
        on-legacy-press                            (rn/use-callback #(set-wallet-type :legacy))
        on-multichain-press                        (rn/use-callback #(set-wallet-type :multichain))
        preferred-networks                         (rf/sub [:wallet/preferred-chain-names-for-address
                                                            address])
        share-title                                (str name " " (i18n/label :t/address))
        qr-url                                     (rn/use-memo #(utils/get-wallet-qr
                                                                  {:wallet-type       wallet-type
                                                                   :selected-networks selected-networks
                                                                   :address           address})
                                                                [wallet-type selected-networks address])
        qr-media-server-uri                        (rn/use-memo
                                                    #(image-server/get-qr-image-uri-for-any-url
                                                      {:url         qr-url
                                                       :port        (rf/sub [:mediaserver/port])
                                                       :qr-size     qr-size
                                                       :error-level :highest})
                                                    [qr-url])]

    (rn/use-mount #(set-selected-networks preferred-networks))

    [quo/overlay {:type :shell}
     [rn/view
      {:flex        1
       :padding-top padding-top}
      [quo/page-nav
       {:icon-name           :i/close
        :on-press            navigate-back
        :background          :blur
        :accessibility-label :top-bar}]
      [quo/page-top
       {:title           (i18n/label :t/share-address)
        :container-style {:margin-bottom 8}}]
      [rn/view {:style {:padding-horizontal 20}}
       [quo/share-qr-code
        {:type                :saved-address
         :address             wallet-type
         :qr-image-uri        qr-media-server-uri
         :qr-data             qr-url
         :networks            selected-networks
         :on-share-press      #(share-action qr-url share-title)
         :profile-picture     nil
         :full-name           name
         :customization-color customization-color
         :on-legacy-press     on-legacy-press
         :on-multichain-press on-multichain-press
         :on-settings-press   on-settings-press}]]]]))
