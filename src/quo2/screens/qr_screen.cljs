(ns quo2.screens.qr-screen
  (:require [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.icons.icons :as icons]
            [re-frame.core :as re-frame]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.copyable-text :as copyable-text]))
;This is the space where we show various selector options to use the QR code component
;I believe following are the options
;
;-> text input for link address
;-> drop down selector to see type of QR code
;with the following options :
;1) profile address
;2) wallet legacy
;3) wallet multichain

(def qr-container-radius 16)
(def static-qr-code-url "https://status.im")
(def profile-link "status.app/u/zQ34e2ahd1835eqacc17f6asas12adjie8")

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


(defn preview-qr []
  (let [window-width @(re-frame/subscribe [:dimensions/window-width])]
    [rn/view {:style {:background-color colors/neutral-80-opa-80}}
      [rn/view {:style (qr-code-container window-width)}
       [qr-code-viewer/qr-code-view (* window-width 0.808) static-qr-code-url 12 colors/white]
       [rn/view {:style profile-address-container}
        [rn/view {:style profile-address-column}
         [rn/text {:style profile-address-label} (i18n/label :t/link-to-profile)]
         [copyable-text/copyable-text-view {:copied-text profile-link
                                            :container-style copyable-text-container-style}
          [rn/text {:style (profile-address-content (* window-width 0.7))
                    :ellipsize-mode :middle
                    :number-of-lines 1}
           profile-link]
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
