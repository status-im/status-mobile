(ns quo2.components.qr
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

;This is the space where the core component would live
;Where all the various options would be passed and depending on the options
;the final view would be rendered
(def tab-container-radius 10)
(def tab-item-height 15)

(def qr-container-radius 16)
(def static-qr-code-url "https://status.im")
(def static-link-to-profile "status.app/u/zQ34e2ahd1835eqacc17f6asas12adjie8")

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

(def address-share-button-container
  {:padding 8
   :position :absolute
   :background-color colors/white-opa-5
   :border-radius 10
   :right 14
   :top 10})

(def profile-address-container
  {:flex-direction :row
   :margin-top 6
   :width :98%})

(def profile-address-column
  {:align-self :flex-start})

(def profile-address-label
  {:color colors/white-opa-40
   :align-self :flex-start
   :padding-horizontal 20
   :padding-top 10
   :font-weight :500
   :font-size 13})

(def copyable-text-container-style
  {:background-color :transparent
   :width :100%})

(defn profile-address-content [max-width]
  {:color colors/white
   :align-self :flex-start
   :padding-horizontal 20
   :padding-top 2
   :font-weight :500
   :font-size 16
   :max-width max-width})

(def information-icon-container
  {:position :absolute
   :right 0})

(def account-subtype-label
  {:color colors/white
   :font-size tab-item-height
   :line-height 22
   :font-weight :500})

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

(def account-subtype-row
  {:flex-direction :row
   :width :89.3%})

(def multichain-address-share-button-container
  {:position :absolute
   :padding 8
   :right 0
   :background-color colors/white-opa-5
   :border-radius 10
   :top 5})

(defn multichain-address-content [max-width]
  {:color colors/white
   :align-self :flex-start
   :padding-top 2
   :font-weight :500
   :font-size 16
   :max-width max-width})

(def seperator-text
  {:color colors/white})

(defn network-text-style [text-color]
  {:color text-color})

(def mulichain-address-string-container
  {:flex-direction :row})

(def mulichain-address-column
  {:flex-direction :column})

(def mulichain-address-container
  {:flex-direction :row
   :width :88%})

(def divider-line-container
  {:border-width 0.5
   :border-color colors/white-opa-20
   :border-style "dashed"
   :width :88%
   :margin-bottom 20})

(def network-share-icon-style
  {:position :absolute
   :padding 8
   :border-radius 10
   :background-color colors/white-opa-5
   :top 10
   :right 0})

(defn wallet-icon-style [first-icon?]
  {:margin-vertical 0
   :border-width 2
   :border-left-color :transparent
   :border-radius 32
   :width 32
   :height 32
   :z-index (if first-icon? 5 99) ;;pathetic attempt to make it look like the icons are cutting into each other
   :margin-left (if first-icon? 0 -10)})

(def wallet-icons-container
  {:flex-direction :row
   :align-self :flex-start
   :padding-vertical 20
   :margin-left 20
   :width :88%})


(defn profile-address-qr-comp [qr-code-url link-to-profile]
  (let [window-width @(re-frame/subscribe [:dimensions/window-width])]
    [rn/view {:style {:background-color colors/neutral-80-opa-80 }}
     [rn/view {:style (qr-code-container window-width)}
      [qr-code-viewer/qr-code-view (* window-width 0.808) qr-code-url 12 colors/white]
      [rn/view {:style profile-address-container}
       [rn/view {:style profile-address-column}
        [rn/text {:style profile-address-label} (i18n/label :t/link-to-profile)]
        [copyable-text/copyable-text-view {:copied-text link-to-profile
                                           :container-style {:background-color :transparent
                                                             :width :100%}}
         [rn/text {:style (profile-address-content (* window-width 0.7))
                   :ellipsize-mode :middle
                   :number-of-lines 1}
          link-to-profile]
         ]
        ]
       [rn/touchable-highlight {:style address-share-button-container}
        [icons/icon :main-icons/share-icon20 {:color colors/white :width 20 :height 20}]
        ]
       ]
      ]
     ]
    )
)

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


(defn legacy-address-qr-comp [qr-url legacy-wallet-address multichain-wallet-address multi-chain-info multichain-wallet-address-with-network-chain]
  (let [selected-sub-account-index (reagent/atom 2)
        window-width @(re-frame/subscribe [:dimensions/window-width])]
    (fn []
      [:<>
       [rn/view {:style {:background-color colors/neutral-80-opa-80 }}
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
               window-width])
        ]]]))
)


(defn qr [{:keys [type url profile wallet-address multichain-wallet-address multi-chain-info multichain-wallet-address-with-network-chain]}]
      (case type :profile [profile-address-qr-comp url profile]
            type :legacy [legacy-address-qr-comp url wallet-address multichain-wallet-address multi-chain-info multichain-wallet-address-with-network-chain]
        )
  )
