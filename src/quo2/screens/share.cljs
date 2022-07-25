(ns quo2.screens.share
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.icons.icons :as icons]
            [re-frame.core :as re-frame]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]))

(def selected-tab-index (reagent/atom 2))
(def selected-account-index (reagent/atom 1))

(def tab-item-height 15)
(def tab-container-height 30)
(def tab-container-radius 10)
(def qr-container-radius 16)
(def emoji-hash-container-radius 16)

;hardcoding some dummy values to visualise the UI
;these variables should be populated from actual values from the database
(def accounts-static-list (list
                           {:id "account-01"
                            :account-text "My Savings Account"
                            :qr-code-url "https://status.im/get/"
                            :legacy-wallet-address "0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
                            :multichain-wallet-address "0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
                            :multichain-wallet-address-with-network-chain "eth:arb:her:opt:zks:0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
                            :multiple-networks-string "eth:arb:her:opt:zks"
                            :account-avatar-text "🍑"
                            :account-theme-color "rgba(134, 97, 193, 1)"
                            :multi-chain-info (list
                                               {:network-type "eth"
                                                :name "Ethereum"
                                                :network-text-color "rgba(76, 180, 239, 1)"
                                                :network-icon-image (resources/get-image :ethereum)}
                                               {:network-type "arb"
                                                :name "Arbitrum"
                                                :network-text-color "rgba(63, 174, 249, 1)"
                                                :network-icon-image (resources/get-image :arbitrum)}
                                               {:network-type "her"
                                                :name "Hermez"
                                                :network-text-color "rgba(251, 143, 97, 1)"
                                                :network-icon-image (resources/get-image :hermez)}
                                               {:network-type "opt"
                                                :name "Optimism"
                                                :network-text-color "rgba(230, 95, 92, 1)"
                                                :network-icon-image (resources/get-image :optimism)}
                                               {:network-type "zks"
                                                :name "zkSync"
                                                :network-text-color "rgba(139, 141, 250, 1)"
                                                :network-icon-image (resources/get-image :zksync)})}

                           {:id "account-02"
                            :account-text "My Current Account"
                            :qr-code-url "https://nimbus.team/"
                            :legacy-wallet-address "0x047050e1c91acc21d166fb6b107b36bdf3b152bff01f69738eae793bae2796a78c5f3f95f52f90641a3745c01b62e56665a5e139a489a19c118e6b7794ae887de2"
                            :multichain-wallet-address "0x0456eebd457cceb9dd709aad83d4fe5a778ab361604b8d9bf25daff91ffcfb821722ada674b42ebdfb0b231c1ef5e599ba9412cacdd24ebd3407b3df971344f585"
                            :multichain-wallet-address-with-network-chain "eth:arb:her:opt:zks:0x0456eebd457cceb9dd709aad83d4fe5a778ab361604b8d9bf25daff91ffcfb821722ada674b42ebdfb0b231c1ef5e599ba9412cacdd24ebd3407b3df971344f585"
                            :multiple-networks-string "eth:arb:her:opt:zks"
                            :account-avatar-text "😇"
                            :account-theme-color "#FA6565"
                            :multi-chain-info (list
                                               {:network-type "eth"
                                                :name "Ethereum"
                                                :network-text-color "rgba(76, 180, 239, 1)"
                                                :network-icon-image (resources/get-image :ethereum)}
                                               {:network-type "arb"
                                                :name "Arbitrum"
                                                :network-text-color "rgba(63, 174, 249, 1)"
                                                :network-icon-image (resources/get-image :arbitrum)}
                                               {:network-type "her"
                                                :name "Hermez"
                                                :network-text-color "rgba(251, 143, 97, 1)"
                                                :network-icon-image (resources/get-image :hermez)}
                                               {:network-type "opt"
                                                :name "Optimism"
                                                :network-text-color "rgba(230, 95, 92, 1)"
                                                :network-icon-image (resources/get-image :optimism)}
                                               {:network-type "zks"
                                                :name "zkSync"
                                                :network-text-color "rgba(139, 141, 250, 1)"
                                                :network-icon-image (resources/get-image :zksync)})}

                           {:id "account-03"
                            :account-text "My Private Account"
                            :qr-code-url "https://messari.io/asset/status/"
                            :legacy-wallet-address "0x04f1ab965a3db6433923ceba0488ff605f230aca4c4c93bb6563c10b700cfdbbed2a364c980b9cf9679f352584d4da44cc01fc1e7257f1d2b80ffff5af64aa129f"
                            :multichain-wallet-address "0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
                            :multichain-wallet-address-with-network-chain "eth:arb:her:opt:zks:0x04a8b2fe1c6388a030f3190bc6f3dc3650f324d049fe459d3d4b1ebab79491ad1f3e84f3190b98d09a6facaebdf8b18f51201219c116c72ea10704470ea9006995"
                            :multiple-networks-string "eth:arb:her:opt:zks"
                            :account-avatar-text "🍑"
                            :account-theme-color "rgba(134, 97, 193, 1)"
                            :multi-chain-info (list
                                               {:network-type "eth"
                                                :name "Ethereum"
                                                :network-text-color "rgba(76, 180, 239, 1)"
                                                :network-icon-image (resources/get-image :ethereum)}
                                               {:network-type "arb"
                                                :name "Arbitrum"
                                                :network-text-color "rgba(63, 174, 249, 1)"
                                                :network-icon-image (resources/get-image :arbitrum)}
                                               {:network-type "her"
                                                :name "Hermez"
                                                :network-text-color "rgba(251, 143, 97, 1)"
                                                :network-icon-image (resources/get-image :hermez)}
                                               {:network-type "opt"
                                                :name "Optimism"
                                                :network-text-color "rgba(230, 95, 92, 1)"
                                                :network-icon-image (resources/get-image :optimism)}
                                               {:network-type "zks"
                                                :name "zkSync"
                                                :network-text-color "rgba(139, 141, 250, 1)"
                                                :network-icon-image (resources/get-image :zksync)})}))

(def profile-link "status.app/u/zQ34e2ahd1835eqacc17f6asas12adjie8")
(def static-ens-name "alisher.stateofus.eth")
(def share-from-ens false)
(def static-wallet-address "0x04e...2ahd1835eqacc17f6")
(def static-qr-code-url "https://status.im")
(def static-emoji-hash "🙈🤭🤓😂🤷🏻😈😇🤑🥳😍🥺😡🍑")

;todo(sid) : move styles to its own seperate file
(defn navigation-text-item [& selected]
  {:font-size 27
   :font-weight :600
   :font-family :inter
   :padding-left 20
   :color (if selected
            colors/white
            colors/white-opa-20)})

(def navigation-top-row
  {:flex-direction :row
   :padding-vertical 26})

(def main-container
  {:background-color colors/neutral-80-opa-80
   :padding-top 40
   :height :100%})

(defn qr-code-container [window-width & wallet-tab]
  {:padding-vertical 20
   :border-radius qr-container-radius
   :margin-top (if wallet-tab 8 20)
   :margin-bottom 4
   :margin-horizontal (* window-width 0.053)
   :width :89.3%
   :background-color colors/white-opa-5
   :flex-direction :column
   :justify-content :center
   :align-items :center})

(def emoji-hash-container
  {:border-radius emoji-hash-container-radius
   :padding-vertical :1%
   :margin-horizontal :5.3%
   :width :89.3%
   :background-color colors/white-opa-5
   :flex-direction :column
   :justify-content :center
   :align-items :center})

(defn emoji-hash-row-container [section-width]
  {:width section-width})

(def tabs-container
  {:flex-direction :row
   :width :89.3%
   :justify-content :space-between
   :align-self :center
   :border-radius tab-container-radius
   :background-color colors/white-opa-5})

(defn tab-item [selected]
  {:height tab-container-height
   :width :50%
   :border-radius tab-container-radius
   :align-items :center
   :justify-content :center
   :background-color (when (= selected true)
                       colors/white-opa-20)})

(def tab-item-text
  {:color colors/white
   :font-size tab-item-height
   :line-height 22
   :font-weight :500})

(def profile-address-label
  {:color colors/white-opa-40
   :align-self :flex-start
   :padding-horizontal 20
   :padding-top 10
   :font-weight :500
   :font-size 13})

(def emoji-hash-label
  {:color colors/white-opa-40
   :align-self :flex-start
   :padding-horizontal 20
   :font-weight :500
   :font-size 14})

(def select-account-label
  {:color colors/white-opa-40
   :align-self :flex-start
   :padding-horizontal :5.3%
   :margin-bottom 5
   :font-weight :500
   :font-size 14})

(defn profile-address-content [max-width]
  {:color colors/white
   :align-self :flex-start
   :padding-horizontal 20
   :padding-top 2
   :font-weight :500
   :font-size 16
   :max-width max-width})

(defn multichain-address-content [max-width]
  {:color colors/white
   :align-self :flex-start
   :padding-top 2
   :font-weight :500
   :font-size 16
   :max-width max-width})

(defn emoji-hash-content [max-width]
  {:color colors/white
   :align-self :flex-start
   :padding-horizontal 20
   :padding-top 4
   :padding-bottom 8
   :font-weight :500
   :font-size 15
   :max-width max-width})

(def address-share-button-container
  {:padding 8
   :position :absolute
   :background-color colors/white-opa-5
   :border-radius 10
   :right 14
   :top 10})

(def emoji-share-button-container
  {:padding 8
   :position :absolute
   :background-color colors/white-opa-5
   :border-radius 10
   :right 14
   :top 2})

(def close-button-container
  {:background-color colors/white-opa-10
   :border-radius 20
   :padding 8})

(def footer-container
  {:flex-direction :row
   :justify-content :center
   :align-items :center
   :height :30%})

(def wallet-footer-container
  {:flex-direction :row
   :justify-content :center
   :align-items :center
   :height :10%})

(def profile-address-container
  {:flex-direction :row
   :margin-top 6
   :width :98%})

(def select-account-section
  {:flex-direction :column
   :margin-top 20})

(def profile-address-column
  {:align-self :flex-start})

(def horizontal-scroll-bar
  {:overflow :scroll})

(defn account-container [window-width]
  {:flex-direction :row
   :width window-width
   :max-width window-width
   :height 40
   :justify-content :flex-start
   :margin-horizontal :5.3%
   :background-color colors/white-opa-5
   :border-radius 10})

(defn account-container-parent [window-width]
  {:width window-width
   :flex-direction :row})

(def account-label-content
  {:color colors/white
   :padding-left 4
   :font-size 16
   :font-weight :500
   :line-height 23
   :align-self :center})

(def account-subtype-label
  {:color colors/white
   :font-size tab-item-height
   :line-height 22
   :font-weight :500})

(def account-subtype-row
  {:flex-direction :row
   :width :89.3%})

(defn account-subtype-tab [selected]
  {:border-radius tab-container-radius
   :align-items :center
   :justify-content :flex-start
   :padding-horizontal 8
   :padding-vertical 3
   :margin-horizontal 5
   :margin-bottom 20
   :background-color (if (= selected true)
                       colors/white-opa-20
                       colors/white-opa-5)})

(def information-icon-container
  {:position :absolute
   :right 0})

(def wallet-icons-container
  {:flex-direction :row
   :align-self :flex-start
   :padding-vertical 20
   :margin-left 20
   :width :88%})

(defn wallet-icon-style [first-icon?]
  {:margin-vertical 0
   :border-width 2
   :border-left-color :transparent
   :border-radius 32
   :width 32
   :height 32
   :z-index (if first-icon? 5 99) ;;pathetic attempt to make it look like the icons are cutting into each other
   :margin-left (if first-icon? 0 -10)})

(def network-share-icon-style
  {:position :absolute
   :padding 8
   :border-radius 10
   :background-color colors/white-opa-5
   :top 10
   :right 0})

(def divider-line-container
  {:border-width 0.5
   :border-color colors/white-opa-20
   :border-style "dashed"
   :width :88%
   :margin-bottom 20})

(def seperator-text
  {:color colors/white})

(def mulichain-address-string-container
  {:flex-direction :row})

(defn network-text-style [text-color]
  {:color text-color})

(def mulichain-address-column
  {:flex-direction :column})

(def mulichain-address-container
  {:flex-direction :row
   :width :88%})

(def multichain-address-share-button-container
  {:position :absolute
   :padding 8
   :right 0
   :background-color colors/white-opa-5
   :border-radius 10
   :top 5})

(defn account-avatar-container [background-color]
  {:background-color background-color
   :width 32
   :height 32
   :border-radius 10
   :justify-content :center
   :align-content :center
   :align-self :center
   :margin 4})

(def account-avatar-content
  {:align-self :center})

(def copyable-text-container-style
  {:background-color :transparent
   :width :100%})

;(def legacy-wallet-address-view-container
;  {:margin-top 10})

(defn profile-tab [window-width]
  [:<>
   [rn/view {:style (qr-code-container window-width)}
    [qr-code-viewer/qr-code-view (* window-width 0.808) static-qr-code-url 12 colors/white]
    [rn/view {:style profile-address-container}
     [rn/view {:style profile-address-column}
      [rn/text {:style profile-address-label} (i18n/label :t/link-to-profile)]
      [copyable-text/copyable-text-view {:copied-text (cond (= share-from-ens true) static-ens-name :else profile-link)
                                         :container-style copyable-text-container-style}
       [rn/text {:style (profile-address-content (* window-width 0.7))
                 :ellipsize-mode :middle
                 :number-of-lines 1}
        (cond (= share-from-ens true) static-ens-name :else profile-link)]]]
     [rn/touchable-highlight {:style address-share-button-container}
      [icons/icon :main-icons/share-icon20 {:color colors/white :width 20 :height 20}]]]]

   (cond (= share-from-ens false) [rn/view {:style emoji-hash-container}
                                   [rn/view {:style profile-address-container}
                                    [rn/view {:style profile-address-column}
                                     [rn/text {:style emoji-hash-label} (i18n/label :t/emoji-hash)]
                                     [copyable-text/copyable-text-view {:copied-text static-emoji-hash :container-style copyable-text-container-style}
                                      [rn/view {:style (emoji-hash-row-container (* window-width 0.87))}
                                       [rn/text {:style (emoji-hash-content (* window-width 0.72))} static-emoji-hash]
                                       [rn/view {:style emoji-share-button-container}
                                        [icons/icon :main-icons/copy-icon20 {:color colors/white :width 20 :height 20}]]]]]]])

   [rn/view {:style footer-container}
    [rn/touchable-highlight {:style close-button-container :on-press #(re-frame/dispatch [:navigate-back])}
     [icons/icon :main-icons/close
      {:color colors/white :width 24 :height 24}]]]])

(defn legacy-wallet-address-view [address-text window-width]
  [rn/view {:style profile-address-container}
   [rn/view {:style profile-address-column}
    [rn/text {:style profile-address-label} (i18n/label :t/wallet-address)]
    [copyable-text/copyable-text-view {:copied-text address-text :container-style copyable-text-container-style}
     [rn/text {:style (profile-address-content (* window-width 0.7)) :ellipsize-mode :middle :number-of-lines 1} address-text]]]
   [rn/touchable-highlight {:style address-share-button-container}
    [icons/icon :main-icons/share-icon20 {:color colors/white :width 20 :height 20}]]])

(defn multichain-wallet-address-view [address-text multi-chain-info multichain-wallet-address-with-network-chain window-width]
  [:<>
   [rn/view {:style wallet-icons-container}
    (for [single-chain multi-chain-info] ^{:key single-chain}
         [react/image {:source (get single-chain :network-icon-image) :style (wallet-icon-style (= single-chain (first multi-chain-info)))}])
    [rn/touchable-highlight {:style network-share-icon-style}
     [icons/icon :main-icons/customize-icon20 {:color colors/white :width 20 :height 20}]]]

   [rn/view {:style divider-line-container}]

   [rn/view {:style mulichain-address-container}
    [rn/view {:style mulichain-address-column}
     [rn/view {:style mulichain-address-string-container}

      (for
       [wallet-item multi-chain-info] ^{:key wallet-item}
       [:<>
        [rn/text {:style (network-text-style (get wallet-item :network-text-color))} (get wallet-item :network-type)]
        (when-not (= wallet-item (last multi-chain-info)) [rn/text {:style seperator-text} ":"])])]

     [copyable-text/copyable-text-view {:copied-text multichain-wallet-address-with-network-chain :container-style copyable-text-container-style}
      [rn/text {:style (multichain-address-content (* window-width 0.6)) :ellipsize-mode :middle :number-of-lines 1} address-text]]]

    [rn/touchable-highlight {:style multichain-address-share-button-container}
     [icons/icon :main-icons/share-icon20 {:color colors/white :width 20 :height 20}]]]])

(defn account-details [qr-url legacy-wallet-address multichain-wallet-address multi-chain-info multichain-wallet-address-with-network-chain window-width]
  (let [selected-sub-account-index (reagent/atom 2)]
    (fn []
      [:<>
       [rn/view {:style (qr-code-container window-width true)}

        [rn/view {:style account-subtype-row}

         [rn/touchable-highlight {:style (account-subtype-tab (= @selected-sub-account-index 1))}
          [rn/text {:style account-subtype-label
                    :on-press #(reset! selected-sub-account-index 1)} (i18n/label :t/legacy)]]

         [rn/touchable-highlight {:style (account-subtype-tab (= @selected-sub-account-index 2))}
          [rn/text {:style account-subtype-label
                    :on-press #(reset! selected-sub-account-index 2)} (i18n/label :t/multichain)]]

         [rn/touchable-highlight {:style information-icon-container}
          [icons/icon :main-icons/info-tooltip
           {:color "rgba(255, 255, 255, 0.4)"
            :width 22
            :height 22}]]]

        [qr-code-viewer/qr-code-view (* window-width 0.808) qr-url 12 colors/white]
        (cond (= @selected-sub-account-index 1)
              [legacy-wallet-address-view
               legacy-wallet-address
               window-width]
              :else
              [multichain-wallet-address-view
               multichain-wallet-address
               multi-chain-info
               multichain-wallet-address-with-network-chain
               window-width])]])))

(defn wallet-tab [window-width]
  [:<>
   [rn/view {:style select-account-section}
    [rn/text {:style select-account-label} (i18n/label :t/select-account)]
    [rn/scroll-view
     {:style                 horizontal-scroll-bar
      :horizontal            true
      :paging-enabled        true
      :snap-to-interval      window-width
      :snap-to-alignment     :start
      :deceleration-rate     "fast"
      :scroll-event-throttle 64
      :on-scroll             #(let [x (.-nativeEvent.contentOffset.x ^js %)]
                                (reset! selected-account-index (Math/max (Math/round (/ x window-width)) 0)))}
     (doall (for [account-info accounts-static-list] ^{:key (get account-info :id)}
                 [rn/view
                  [rn/view {:style (account-container-parent window-width)}
                   [rn/view {:style (account-container (* window-width 0.89))}
                    [rn/view {:style (account-avatar-container (get account-info :account-theme-color))}
                     [rn/text {:style account-avatar-content} (get account-info :account-avatar-text)]]
                    [rn/text {:style account-label-content} (get account-info :account-text)]]]

                  [account-details
                   (get account-info :qr-code-url)
                   (get account-info :legacy-wallet-address)
                   (get account-info :multichain-wallet-address)
                   (get account-info :multi-chain-info)
                   (get account-info :multichain-wallet-address-with-network-chain)
                   window-width]]))]]

   [rn/view {:style wallet-footer-container}
    [rn/touchable-highlight {:style close-button-container :on-press #(re-frame/dispatch [:navigate-back])}
     [icons/icon :main-icons/close
      {:color colors/white :width 24 :height 24}]]]])

(defn share-view []
  (let [window-width @(re-frame/subscribe [:dimensions/window-width])]
    [rn/view {:style main-container}
     [rn/view {:style navigation-top-row}
      [rn/text {:style (navigation-text-item)} (i18n/label :t/switcher-nav-switch)]
      [rn/text {:style (navigation-text-item)} (i18n/label :t/switcher-nav-scan)]
      [rn/text {:style (navigation-text-item true)} (i18n/label :t/switcher-nav-share)]
      [rn/text {:style (navigation-text-item)} (i18n/label :t/switcher-nav-activity)]]
     [rn/scroll-view
      [rn/view {:style tabs-container}
       [rn/touchable-highlight
        {:style (tab-item (= @selected-tab-index 1))
         :on-press #(reset! selected-tab-index 1)}
        [rn/text {:style tab-item-text} (i18n/label :t/profile)]]

       [rn/touchable-highlight
        {:style (tab-item (= @selected-tab-index 2))
         :on-press #(reset! selected-tab-index 2)}
        [rn/text {:style tab-item-text} (i18n/label :t/wallet)]]]
      [rn/view
       (cond (= @selected-tab-index 1) [profile-tab window-width] :else [wallet-tab window-width])]]]))
