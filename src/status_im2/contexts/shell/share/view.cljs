(ns status-im2.contexts.shell.share.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.shell.share.style :as style]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [status-im.ui.components.list-selection :as list-selection]
            [utils.image-server :as image-server]
            [react-native.navigation :as navigation]
            [clojure.string :as string]
            [utils.address :as address]
            [react-native.platform :as platform]))

(defn header
  []
  [:<>
   [rn/view {:style style/header-row}
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :close-shell-share-tab
      :container-style     style/header-button
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/close]
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :shell-scan-button
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/scan]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-heading}
    (i18n/label :t/share)]])

(defn profile-tab
  [window-width]
  (let [{:keys [emoji-hash
                compressed-key
                key-uid]} (rf/sub [:profile/profile])
        port              (rf/sub [:mediaserver/port])
        emoji-hash-string (string/join emoji-hash)
        qr-size           (int (- window-width 64))
        abbreviated-url   (address/get-abbreviated-profile-url
                           image-server/status-profile-base-url-without-https
                           compressed-key)
        profile-url       (str image-server/status-profile-base-url compressed-key)
        source-uri        (image-server/get-account-qr-image-uri
                           {:key-uid    key-uid
                            :public-key compressed-key
                            :port       port
                            :qr-size    qr-size})]
    [:<>
     [rn/view {:style style/qr-code-container}
      [quo/qr-code
       {:source {:uri source-uri}
        :width  qr-size
        :height qr-size}]
      [rn/view {:style style/profile-address-container}
       [rn/view {:style style/profile-address-column}
        [quo/text
         {:size   :paragraph-2
          :weight :medium
          :style  style/profile-address-label}
         (i18n/label :t/link-to-profile)]
        [rn/touchable-highlight
         {:active-opacity   1
          :underlay-color   colors/neutral-80-opa-1-blur
          :background-color :transparent
          :on-press         #(rf/dispatch [:share/copy-text-and-show-toast
                                           {:text-to-copy      profile-url
                                            :post-copy-message (i18n/label :t/link-to-profile-copied)}])
          :on-long-press    #(rf/dispatch [:share/copy-text-and-show-toast
                                           {:text-to-copy      profile-url
                                            :post-copy-message (i18n/label :t/link-to-profile-copied)}])}
         [quo/text
          {:style           style/profile-address-content
           :size            :paragraph-1
           :weight          :medium
           :ellipsize-mode  :middle
           :number-of-lines 1}
          abbreviated-url]]]
       [rn/view {:style style/share-button-container}
        [quo/button
         {:icon-only?          true
          :type                :grey
          :background          :blur
          :size                32
          :accessibility-label :link-to-profile
          :on-press            #(list-selection/open-share {:message profile-url})}
         :i/share]]]]

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

(defn wallet-tab
  []
  [rn/text {:style style/wip-style} "not implemented"])

(defn tab-content
  [window-width]
  (let [selected-tab (reagent/atom :profile)]
    (fn []
      [:<>
       [header]
       [rn/view {:style style/tabs-container}
        [quo/segmented-control
         {:size           28
          :blur?          true
          :on-change      #(reset! selected-tab %)
          :default-active :profile
          :data           [{:id    :profile
                            :label (i18n/label :t/profile)}
                           {:id    :wallet
                            :label (i18n/label :t/wallet)}]}]]
       (if (= @selected-tab :profile)
         [profile-tab window-width]
         [wallet-tab])])))

(defn view
  []
  (let [window-width (rf/sub [:dimensions/window-width])]
    (fn []
      [rn/view
       {:flex        1
        :padding-top (navigation/status-bar-height)}
       [blur/view
        {:style       style/blur
         :blur-amount 20
         :blur-radius (if platform/android? 25 10)}]
       [tab-content window-width]])))
