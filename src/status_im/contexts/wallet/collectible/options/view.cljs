(ns status-im.contexts.wallet.collectible.options.view
  (:require
    [quo.core :as quo]
    [status-im.contexts.wallet.common.utils.external-links :as external-links]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn view
  [{:keys [image name chain-id address]}]
  (let [uri (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-etherscan
        :on-press            (fn []
                               (rf/dispatch [:wallet/navigate-to-chain-explorer-from-bottom-sheet
                                             (external-links/get-explorer-url-by-chain-id chain-id)
                                             address]))
        :label               (i18n/label :t/view-on-eth)
        :right-icon          :i/external}]
      [{:icon                :i/save
        :accessibility-label :save-image
        :label               (i18n/label :t/save-image-to-photos)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch
                                [:lightbox/save-image-to-gallery
                                 uri
                                 #(rf/dispatch [:toasts/upsert
                                                {:id   :random-id
                                                 :type :positive
                                                 :text (i18n/label :t/photo-saved)}])]))}]
      [{:icon                :i/share
        :accessibility-label :share-collectible
        :label               (i18n/label :t/share-collectible)
        :on-press            #(rf/dispatch [:wallet/share-collectible
                                            {:in-sheet? true
                                             :title     name
                                             :uri       uri}])}]]]))
