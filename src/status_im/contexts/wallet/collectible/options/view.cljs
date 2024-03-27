(ns status-im.contexts.wallet.collectible.options.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn view
  [image]
  (let [uri             (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-etherscan
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
                                                {:id              :random-id
                                                 :type            :positive
                                                 :container-style {:bottom (when platform/android? 20)}
                                                 :text            (i18n/label :t/photo-saved)}])]))}]
      [{:icon                :i/share
        :accessibility-label :share-collectible
        :label               (i18n/label :t/share-collectible)
        }]]]))