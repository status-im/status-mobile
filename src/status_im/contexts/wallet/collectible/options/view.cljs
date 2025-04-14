(ns status-im.contexts.wallet.collectible.options.view
  (:require
    [quo.core :as quo]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn view
  [{:keys [image name id]}]
  (let [chain-id                      (get-in id [:contract-id :chain-id])
        {:keys [block-explorer-name]} (rf/sub [:wallet/network-details-by-chain-id chain-id])
        token-id                      (:token-id id)
        contract-address              (get-in id [:contract-id :address])
        uri                           (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-block-explorer
        :on-press            (fn []
                               (rf/dispatch [:wallet/navigate-to-chain-explorer chain-id
                                             contract-address]))
        :label               (i18n/label :t/view-on-block-explorer
                                         {:block-explorer-name block-explorer-name})
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
                                            {:token-id         token-id
                                             :contract-address contract-address
                                             :chain-id         chain-id
                                             :title            name}])}]]]))

"222"
"https://nft-cdn.alchemy.com/eth-mainnet/219530f9b3a7901f02169334d593823e"
"Tengria #913"
{:contract-id {:chain-id 1 :address "0x1a4ceef5d575c2228d142ef862a9b60be8161e7f"} :token-id "913"}

{:contract-id {:chain-id 1 :address "0x1a4ceef5d575c2228d142ef862a9b60be8161e7f"} :token-id "913"}
