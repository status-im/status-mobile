(ns status-im.contexts.profile.contact.share.view
  (:require [legacy.status-im.ui.components.list-selection :as list-selection]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
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
        abbreviated-url (rn/use-memo (fn []
                                       (address/get-abbreviated-profile-url universal-profile-url))
                                     [universal-profile-url])
        profile-picture (rn/use-memo #(profile.utils/photo profile) [profile])
        display-name    (rn/use-memo #(profile.utils/displayed-name profile) [profile])
        on-back-press   #(rf/dispatch [:navigate-back])
        on-share-press  (rn/use-callback #(list-selection/open-share {:message universal-profile-url})
                                         [universal-profile-url])
        on-copy-press   (rn/use-callback (fn []
                                           (rf/dispatch [:share/copy-text-and-show-toast
                                                         {:text-to-copy universal-profile-url
                                                          :post-copy-message
                                                          (i18n/label
                                                           :t/link-to-profile-copied)}]))
                                         [universal-profile-url])]
    [quo/overlay {:type :shell}
     [rn/view
      {:style {:padding-top (safe-area/get-top)}
       :key   :share-community}
      [quo/page-nav
       {:icon-name           :i/close
        :on-press            on-back-press
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
         :profile-picture     profile-picture
         :full-name           display-name
         :customization-color (or customization-color :blue)}]]]]))
