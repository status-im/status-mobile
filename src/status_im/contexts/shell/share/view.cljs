(ns status-im.contexts.shell.share.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.list-selection :as list-selection]
    [legacy.status-im.ui.components.react :as react]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [react-native.share :as share]
    [reagent.core :as reagent]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.contexts.profile.utils :as profile.utils]
    [status-im.contexts.shell.share.style :as style]
    [status-im.contexts.wallet.common.sheets.network-preferences.view :as network-preferences]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.address :as address]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
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

(def qr-size 500)

(defn- share-action
  [address share-title]
  (share/open
   (if platform/ios?
     {:activity-item-sources [{:placeholder-item {:type    "text"
                                                  :content address}
                               :item             {:default {:type "text"
                                                            :content
                                                            address}}
                               :link-metadata    {:title share-title}}]}
     {:title   share-title
      :subject share-title
      :message address})))


(defn- open-preferences
  [selected-networks]
  (rf/dispatch [:show-bottom-sheet
                {:theme :dark
                 :shell? true
                 :content
                 (fn []
                   [network-preferences/view
                    {:blur?             true
                     :selected-networks (set @selected-networks)
                     :on-save           (fn [chain-ids]
                                          (rf/dispatch [:hide-bottom-sheet])
                                          (reset! selected-networks (map #(get utils/id->network %)
                                                                         chain-ids)))}])}]))
(defn wallet-qr-code-item
  [account]
  (let [selected-networks (reagent/atom [:ethereum :optimism :arbitrum])
        wallet-type       (reagent/atom :wallet-legacy)
        width           (rf/sub [:dimensions/window-width])]
    (fn []
      (let [share-title         (str (:name account) " " (i18n/label :t/address))
            qr-url              (utils/get-wallet-qr {:wallet-type       @wallet-type
                                                      :selected-networks @selected-networks
                                                      :address           (:address account)})
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         qr-url
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     qr-size
                                  :error-level :highest})]
        [rn/view {:style {:width width}}
         [rn/view { :style style/qr-code-container}
          [quo/share-qr-code
           {:type                @wallet-type
            :qr-image-uri        qr-media-server-uri
            :qr-data             qr-url
            :networks            @selected-networks
            :on-share-press      #(share-action qr-url share-title)
            :profile-picture     nil
            :unblur-on-android?  true
            :full-name           (:name account)
            :customization-color (:color account)
            :emoji               (:emoji account)
            :on-multichain-press #(reset! wallet-type :wallet-multichain)
            :on-legacy-press     #(reset! wallet-type :wallet-legacy)
            :on-settings-press   #(open-preferences selected-networks)}]]]
        ))))

(defn wallet-tab
  []
  (let [accounts (rf/sub [:wallet/accounts])]
    [react/scroll-view {:horizontal true :style {:flex 1}} 
     (for [account accounts]
       [wallet-qr-code-item account {:key (:key-uid account)}])]))

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
       (if (= @selected-tab :profile)
         [profile-tab]
         [wallet-tab])])))

(defn view
  []
  [rn/view {:flex 1 :padding-top (safe-area/get-top)}
   [blur/view
    {:style       style/blur
     :blur-amount 20
     :blur-radius (if platform/android? 25 10)}]
   [tab-content]])
