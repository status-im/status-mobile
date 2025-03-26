(ns status-im.contexts.settings.wallet.saved-addresses.share-address.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.contexts.settings.wallet.saved-addresses.share-address.style :as style]
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

(defn view
  []
  (let [{:keys [name address customization-color]} (quo.theme/use-screen-params)
        share-title                                (str name " " (i18n/label :t/address))
        qr-url                                     address
        qr-media-server-uri                        (rn/use-memo
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
         :qr-image-uri        qr-media-server-uri
         :qr-data             qr-url
         :on-share-press      #(share-action qr-url share-title)
         :full-name           name
         :customization-color customization-color}]]]]))
