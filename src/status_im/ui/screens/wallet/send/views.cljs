(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as str]
            [status-im.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.components.button.view :as button]
            [status-im.components.styles :as styles]
            [status-im.components.status-bar :as status-bar]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.camera :as camera]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.wallet.send.styles :as send.styles]
            [status-im.components.icons.vector-icons :as vector-icons]
            [reagent.core :as r]))

(defn- show-not-implemented! []
  (utils/show-popup "TODO" "Not implemented yet!"))

(defn toolbar-view []
  [toolbar/toolbar2 {:style send.styles/toolbar}
   [toolbar/nav-button (act/close-white act/default-handler)]
   [toolbar/content-title {:color :white} (i18n/label :t/wallet-send-transaction)
    {:color styles/color-light-blue} (i18n/label :t/wallet-send-step-one)]
   [toolbar/actions [{:icon      :icons/flash-active
                      :icon-opts {:color :white}
                      :handler   show-not-implemented!}]]])

(defn recipient-buttons []
  [react/view {:style send.styles/recipient-buttons}
   [react/touchable-highlight {:style (send.styles/recipient-touchable true)}
    [react/view {:style send.styles/recipient-button}
     [react/text {:style send.styles/recipient-button-text}
      (i18n/label :t/wallet-choose-from-contacts)]
     [vector-icons/icon :icons/contacts {:color           :white
                                         :container-style send.styles/recipient-icon}]]]
   [react/touchable-highlight {:style (send.styles/recipient-touchable true)
                               :on-press #(react/get-from-clipboard
                                           (fn [clipboard]
                                             (re-frame/dispatch [:choose-recipient clipboard])))}
    [react/view {:style send.styles/recipient-button}
     [react/text {:style send.styles/recipient-button-text}
      (i18n/label :t/wallet-address-from-clipboard)]
     [vector-icons/icon :icons/copy-from {:color           :white
                                          :container-style send.styles/recipient-icon}]]]
   [react/touchable-highlight {:style (send.styles/recipient-touchable false)}
    [react/view {:style send.styles/recipient-button}
     [react/text {:style send.styles/recipient-button-text}
      (i18n/label :t/wallet-browse-photos)]
     [vector-icons/icon :icons/browse {:color           :white
                                       :container-style send.styles/recipient-icon}]]]])

(defview send-transaction []
  (letsubs [camera-dimensions [:camera-dimensions]]
    [react/view {:style send.styles/wallet-container}
     [status-bar/status-bar {:type :wallet}]
     [toolbar-view]
     [react/view {:style send.styles/choose-recipient-container}
      [react/text {:style send.styles/choose-recipient-label} (i18n/label :t/wallet-choose-recipient)]]
            [react/view {:style     send.styles/qr-container
                         :on-layout #(let [layout (.. % -nativeEvent -layout)]
                                    (re-frame/dispatch [:set-in [:wallet :camera-dimensions]
                                                        {:width  (.-width layout)
                                                         :height (.-height layout)}]))}
             [camera/camera {:style         send.styles/preview
                             :aspect        :fill
                             :captureAudio  false
                             :onBarCodeRead (fn [code]
                                              (let [data (-> code
                                                             .-data
                                                             (str/replace #"ethereum:" ""))]
                                                (re-frame/dispatch [:choose-recipient data])))}]
             [react/view {:style (send.styles/outer-bezel camera-dimensions)}]
             [react/view {:style (send.styles/inner-bezel camera-dimensions)}]]
     [recipient-buttons]]))
