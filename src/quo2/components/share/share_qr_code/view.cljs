(ns quo2.components.share.share-qr-code.view
  (:require
    [quo2.components.buttons.button.view :as button]
    [quo2.components.markdown.text :as text]
    [quo2.components.share.qr-code.view :as qr-code]
    [quo2.components.share.share-qr-code.style :as style]
    [quo2.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]))

(defn view
  [{:keys [qr-image-uri link-title url-on-press url-on-long-press qr-url share-on-press]}]
  [blur/ios-view
   {:style     style/qr-code-container
    :blur-type :light}
   [qr-code/view {:qr-image-uri qr-image-uri}]
   [rn/view {:style style/profile-address-container}
    [rn/view {:style style/profile-address-column}
     [text/text
      {:size   :paragraph-2
       :weight :medium
       :style  style/profile-address-label}
      link-title]
     [rn/touchable-highlight
      {:active-opacity   1
       :underlay-color   colors/neutral-80-opa-1-blur
       :background-color :transparent
       :on-press         url-on-press
       :on-long-press    url-on-long-press
       :style            style/profile-address-content-container}
      [text/text
       {:style           style/profile-address-content
        :size            :paragraph-1
        :weight          :medium
        :ellipsize-mode  :middle
        :number-of-lines 1}
       qr-url]]]
    [rn/view {:style style/share-button-container}
     [button/button
      {:icon-only?          true
       :type                :grey
       :background          :blur
       :size                32
       :accessibility-label :share-profile
       :on-press            share-on-press}
      :i/share]]]])
