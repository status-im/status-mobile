(ns status-im.contexts.shell.share.profile.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.list-selection :as list-selection]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.contexts.shell.share.style :as style]
    [utils.address :as address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn profile-tab
  []
  (let [{:keys [emoji-hash
                customization-color
                universal-profile-url]
         :as   profile}   (rf/sub [:profile/profile])
        abbreviated-url   (address/get-abbreviated-profile-url
                           universal-profile-url)
        emoji-hash-string (string/join emoji-hash)]
    [rn/scroll-view
     [rn/view {:style style/qr-code-container}
      [qr-codes/share-qr-code
       {:type                :profile
        :unblur-on-android?  true
        :qr-data             universal-profile-url
        :qr-data-label-shown abbreviated-url
        :on-share-press      #(list-selection/open-share {:message universal-profile-url})
        :on-text-press       #(rf/dispatch [:share/copy-text-and-show-toast
                                            {:text-to-copy      universal-profile-url
                                             :post-copy-message (i18n/label :t/link-to-profile-copied)}])
        :on-text-long-press  #(rf/dispatch [:share/copy-text-and-show-toast
                                            {:text-to-copy      universal-profile-url
                                             :post-copy-message (i18n/label :t/link-to-profile-copied)}])
        :profile-picture     (:uri (profile.utils/photo profile))
        :full-name           (profile.utils/displayed-name profile)
        :customization-color customization-color}]]

     [rn/view {:style style/emoji-hash-container}
      [rn/view {:style style/emoji-address-container}
       [rn/view {:style style/emoji-address-column}
        [quo/text
         {:size   :paragraph-2
          :weight :medium
          :style  style/emoji-hash-label}
         (i18n/label :t/emoji-hash)]
        [rn/touchable-highlight
         {:active-opacity   1
          :underlay-color   colors/neutral-80-opa-1-blur
          :background-color :transparent
          :on-press         #(rf/dispatch [:share/copy-text-and-show-toast
                                           {:text-to-copy      emoji-hash-string
                                            :post-copy-message (i18n/label :t/emoji-hash-copied)}])
          :on-long-press    #(rf/dispatch [:share/copy-text-and-show-toast
                                           {:text-to-copy      emoji-hash-string
                                            :post-copy-message (i18n/label :t/emoji-hash-copied)}])}
         [rn/text {:style style/emoji-hash-content} emoji-hash-string]]]]
      [rn/view {:style style/emoji-share-button-container}
       [quo/button
        {:icon-only?          true
         :type                :grey
         :background          :blur
         :size                32
         :accessibility-label :link-to-profile
         :container-style     {:margin-right 12}
         :on-press            #(rf/dispatch [:share/copy-text-and-show-toast
                                             {:text-to-copy      emoji-hash-string
                                              :post-copy-message (i18n/label :t/emoji-hash-copied)}])
         :on-long-press       #(rf/dispatch [:share/copy-text-and-show-toast
                                             {:text-to-copy      emoji-hash-string
                                              :post-copy-message (i18n/label :t/emoji-hash-copied)}])}
        :i/copy]]]]))
