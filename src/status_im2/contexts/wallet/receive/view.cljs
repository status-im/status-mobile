(ns status-im2.contexts.wallet.receive.view
  (:require
    [quo.core :as quo]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.wallet.receive.style :as style]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def ^:private profile-link
  "https://status.app/u/CwWACgkKB0VlZWVlZWUD#zQ3shUeRSwU6rnUk5JfK2k5HRiM5Hy3wU3UZQrKVzopmAHcQv")

(def ^:private wallet-address "0x39cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2")

(def possible-networks [:ethereum :optimism :arbitrum :myNet])

(defn- get-network-short-name-url
  [network]
  (case network
    :ethereum "eth:"
    :optimism "opt:"
    :arbitrum "arb1:"
    (str (name network) ":")))


(defn view
  []
  (let [padding-top         (:top (safe-area/get-insets))
        qr-url              (as-> (take 2 possible-networks) $
                                  (map get-network-short-name-url $)
                                  (apply str $)
                                  (str $ profile-link))
        qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                              {:url         qr-url
                               :port        (rf/sub [:mediaserver/port])
                               :qr-size     500
                               :error-level :highest})]
    [rn/view {:flex        1
              :padding-top padding-top}
     [blur/view
      {:style       style/blur
       :blur-amount 20
       :blur-radius (if platform/android? 25 10)}]
     [quo/page-nav
      {:icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :background          :blur
       :right-side          [{:icon-name :i/scan}]
       :accessibility-label :top-bar}]
     [quo/text-combinations
      {:container-style style/header-container
       :title           (i18n/label :t/receive)}]
     [rn/view {:style {:padding-horizontal 20}}
      [quo/share-qr-code
       {:type                :wallet-multichain
        ;:qr-data             profile-link
        :on-share-press      #(js/alert "share pressed")
        :on-text-press       #(js/alert "text pressed")
        :on-text-long-press  #(js/alert "text long press")
        :profile-picture     nil
        :unblur-on-android?  true
        :full-name           "My User"
        :customization-color :purple
        :emoji               "🐈"
        :on-info-press       #(js/alert "Info pressed")
        :on-legacy-press     #(js/alert (str "Tab " % " pressed"))
        :on-multichain-press #(js/alert (str "Tab " % " pressed"))
        :networks            (take 2 possible-networks)
        :on-settings-press   #(js/alert "Settings pressed")
        :qr-image-uri        qr-media-server-uri
        :qr-data             qr-url
        }]]]))
