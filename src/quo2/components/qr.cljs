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

(defn qr
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
  )
