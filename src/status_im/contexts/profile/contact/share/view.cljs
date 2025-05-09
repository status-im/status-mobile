(ns status-im.contexts.profile.contact.share.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.qr-codes.view :as qr-codes]
            [status-im.contexts.profile.contact.share.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.address :as address]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [universal-profile-url customization-color]
         :as   profile} (rf/sub [:contacts/current-contact])
        abbreviated-url (rn/use-memo
                         (fn []
                           (address/get-abbreviated-profile-url universal-profile-url))
                         [universal-profile-url])
        on-share-press  (rn/use-callback
                         #(rf/dispatch [:open-share
                                        {:options {:message universal-profile-url}}])
                         [universal-profile-url])
        on-copy-press   (rn/use-callback
                         (fn []
                           (rf/dispatch
                            [:share/copy-text-and-show-toast
                             {:text-to-copy      universal-profile-url
                              :post-copy-message (i18n/label :t/link-to-profile-copied)}]))
                         [universal-profile-url])]
    [quo/overlay {:type :shell}
     [rn/view
      {:style {:padding-top safe-area/top}
       :key   :share-community}
      [quo/page-nav
       {:icon-name           :i/close
        :on-press            events-helper/navigate-back
        :background          :blur
        :accessibility-label :top-bar}]
      [quo/page-top
       {:container-style style/header-heading
        :title           (i18n/label :t/share-profile)}]
      [rn/view {:style style/qr-code-container}
       [qr-codes/share-qr-code
        {:type                :profile
         :qr-data             universal-profile-url
         :qr-data-label-shown abbreviated-url
         :on-share-press      on-share-press
         :on-text-press       on-copy-press
         :on-text-long-press  on-copy-press
         :profile-picture     (profile.utils/photo profile)
         :full-name           (profile.utils/displayed-name profile)
         :customization-color (or customization-color :blue)}]]]]))
