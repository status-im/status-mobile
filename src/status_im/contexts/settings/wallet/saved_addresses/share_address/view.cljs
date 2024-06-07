(ns status-im.contexts.settings.wallet.saved-addresses.share-address.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.contexts.settings.wallet.saved-addresses.share-address.style :as style]
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
  [{:keys [address color selected-networks set-selected-networks]}]
  (let [on-save       (fn [chain-ids]
                        (rf/dispatch [:hide-bottom-sheet])
                        (set-selected-networks (map network-utils/id->network chain-ids)))
        sheet-content (fn []
                        [network-preferences/view
                         {:description       (i18n/label
                                              :t/saved-address-network-preference-selection-description)
                          :button-label      (i18n/label :t/display)
                          :blur?             true
                          :selected-networks (set selected-networks)
                          :account           {:address address
                                              :color   color}
                          :on-save           on-save}])]
    (rf/dispatch [:show-bottom-sheet
                  {:theme   :dark
                   :shell?  true
                   :content sheet-content}])))

(defn view
  []
  (let [{:keys [name address customization-color
                network-preferences-names]}       (rf/sub [:get-screen-params])
        [wallet-type set-wallet-type]             (rn/use-state :legacy)
        [selected-networks set-selected-networks] (rn/use-state network-preferences-names)
        on-settings-press                         (rn/use-callback
                                                   #(open-preferences
                                                     {:selected-networks     selected-networks
                                                      :set-selected-networks set-selected-networks
                                                      :address               address
                                                      :color                 customization-color})
                                                   [address customization-color selected-networks])
        on-legacy-press                           (rn/use-callback #(set-wallet-type :legacy))
        on-multichain-press                       (rn/use-callback #(set-wallet-type :multichain))
        share-title                               (str name " " (i18n/label :t/address))
        qr-url                                    (rn/use-memo #(utils/get-wallet-qr
                                                                 {:wallet-type       wallet-type
                                                                  :selected-networks selected-networks
                                                                  :address           address})
                                                               [wallet-type selected-networks address])
        qr-media-server-uri                       (rn/use-memo
                                                   #(image-server/get-qr-image-uri-for-any-url
                                                     {:url         qr-url
                                                      :port        (rf/sub [:mediaserver/port])
                                                      :qr-size     qr-size
                                                      :error-level :highest})
                                                   [qr-url])]
    [quo/overlay
     {:type       :shell
      :top-inset? true}
     [rn/view {:style style/screen-container}
      [quo/page-nav
       {:icon-name           :i/close
        :on-press            navigate-back
        :background          :blur
        :accessibility-label :top-bar}]
      [quo/page-top
       {:title           (i18n/label :t/share-address)
        :container-style style/top-container}]
      [rn/view {:style style/qr-wrapper}
       [quo/share-qr-code
        {:type                :saved-address
         :address             wallet-type
         :qr-image-uri        qr-media-server-uri
         :qr-data             qr-url
         :networks            selected-networks
         :on-share-press      #(share-action qr-url share-title)
         :full-name           name
         :customization-color customization-color
         :on-legacy-press     on-legacy-press
         :on-multichain-press on-multichain-press
         :on-settings-press   on-settings-press}]]]]))
