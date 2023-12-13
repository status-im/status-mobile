(ns status-im2.contexts.shell.share.view
  (:require
    [oops.core :refer [oget]]
    [react-native.async-storage :as async-storage]
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.common.resources :as resources]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.ui.components.list-selection :as list-selection]
    [status-im2.common.qr-codes.view :as qr-codes]
    [status-im2.contexts.onboarding.common.carousel.animation :as carousel.animation]
    [status-im2.contexts.onboarding.common.carousel.view :as carousel]
    [status-im2.contexts.profile.utils :as profile.utils]
    [status-im2.contexts.shell.jump-to.state :as shell.state]
    [status-im2.contexts.shell.share.style :as style]
    [utils.address :as address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

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
  []
  (let [{:keys [emoji-hash
                customization-color
                universal-profile-url]
         :as   profile}   (rf/sub [:profile/profile])
        abbreviated-url   (address/get-abbreviated-profile-url
                           universal-profile-url)
        emoji-hash-string (string/join emoji-hash)]
    [:<>
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

(def header-text
  [{:text     (i18n/label :t/join-decentralised-communities)
    :sub-text (i18n/label :t/participate-in-the-metaverse)}
   {:text     (i18n/label :t/chat-with-friends)
    :sub-text (i18n/label :t/with-full-encryption)}
   {:text     (i18n/label :t/own-your-crypto)
    :sub-text (i18n/label :t/use-the-multichain-wallet)}
   {:text     (i18n/label :t/discover-web3)
    :sub-text (i18n/label :t/explore-the-decentralized-web)}])

(defn background-image
      [content-width]
      [rn/image
       {:style  {:resize-mode   :stretch
                 :resize-method :scale
                 :margin-top    32
                 :width         content-width}
        :source (resources/get-image :onboarding-illustration)}])

(defonce progress (atom nil))
(defonce paused? (atom nil))
(defonce is-dragging? (atom nil))
(defonce drag-amount (atom nil))

(defn store-screen-height
      [evt]
      (let [window-height (:height (rn/get-window))
            height        (or (oget evt "nativeEvent" "layout" "height") 0)
            width         (or (oget evt "nativeEvent" "layout" "width") 0)]
           ;; Layout height calculation
           ;; 1. Make sure height is more than width, and on-layout is not fired while the
           ;; screen is horizontal
           ;; 2. Initialize values with 0 in case of nil
           ;; 3. In the case of notch devices, the dimensions height will be smaller than
           ;; on-layout,
           ;; (without status bar height included)
           ;; https://github.com/status-im/status-mobile/issues/14633
           ;; 4. In the case of devices without a notch, both heights should be the same,
           ;; but actual values differ in some pixels, so arbitrary 5 pixels is allowed
           (when (and (> height width)
                      (>= (+ height 5) (or window-height 0))
                      (not= height @shell.state/screen-height))
                 (reset! shell.state/screen-height height)
                 (async-storage/set-item! :screen-height height))))

(defn wallet-tab
  []
      (let [view-id      (rf/sub [:view-id])
            animate?     true
            window-width (rf/sub [:dimensions/window-width])]
           (when animate?
                 (carousel.animation/use-initialize-animation progress paused? animate? is-dragging? drag-amount))

           (rn/use-effect
             (fn []
                 (reanimated/set-shared-value @paused? (not= view-id :intro))
                 (fn []
                     (when (= view-id :generating-keys)
                           (carousel.animation/cleanup-animation progress paused?))))
             [view-id])

           [rn/view
            {:style     style/background-container
             :on-layout store-screen-height}
            [carousel/view
             {:animate?          animate?
              :progress          progress
              :paused?           false
              :header-text       header-text
              :is-dragging?      is-dragging?
              :drag-amount       drag-amount
              :header-background true
              :gesture           :swipeable
              :background        [background-image (* 4 window-width)]}]]))
;[rn/view {:style style/wallet-tab-container}]

(defn tab-content
  []
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
       ;(if (= @selected-tab :profile)
       ;  [profile-tab]
       ;  [wallet-tab])
       [wallet-tab]
       ])))

(defn view
  []
  [rn/view {:flex 1 :padding-top (safe-area/get-top)}
   [blur/view
    {:style       style/blur
     :blur-amount 20
     :blur-radius (if platform/android? 25 10)}]
   [tab-content]])
