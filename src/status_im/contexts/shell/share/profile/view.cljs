(ns status-im.contexts.shell.share.profile.view
  (:require
    [clojure.string :as string]
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
                universal-profile-url]
         :as   profile}       (rf/sub [:profile/profile-with-image])
        {window-width :width} (rn/get-window)
        customization-color   (rf/sub [:profile/customization-color])
        abbreviated-url       (address/get-abbreviated-profile-url
                               universal-profile-url)
        emoji-hash-string     (string/join emoji-hash)]
    [rn/scroll-view
     {:content-container-style {:padding-bottom 16}}
     [rn/view {:style style/qr-code-container}
      [qr-codes/share-qr-code
       {:type                :profile
        :width               (- window-width (* style/screen-padding 2))
        :qr-data             universal-profile-url
        :qr-data-label-shown abbreviated-url
        :on-share-press      #(rf/dispatch [:open-share {:options {:message universal-profile-url}}])
        :on-text-press       #(rf/dispatch [:share/copy-text-and-show-toast
                                            {:text-to-copy      universal-profile-url
                                             :post-copy-message (i18n/label :t/link-to-profile-copied)}])
        :on-text-long-press  #(rf/dispatch [:share/copy-text-and-show-toast
                                            {:text-to-copy      universal-profile-url
                                             :post-copy-message (i18n/label :t/link-to-profile-copied)}])
        :profile-picture     (profile.utils/photo profile)
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
