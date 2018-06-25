(ns status-im.ui.screens.desktop.main.tabs.profile.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.user.views :as profile]
            [status-im.utils.build :as build]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [clojure.string :as string]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.screens.desktop.main.tabs.profile.styles :as styles]
            [status-im.ui.screens.profile.user.views :as profile]))

(defn profile-badge [{:keys [name]}]
  [react/view {:margin-vertical 10}
   [react/text {:style {:font-weight :bold}
                :number-of-lines 1}
    name]])

(views/defview copied-tooltip []
  [react/view {:style {:flex-direction :row
                       :justify-content :space-between
                       :align-items :center
                       :height 24
                       :border-radius 8
                       :padding-left 10
                       :padding-right 10
                       :background-color (colors/alpha colors/tooltip-green 0.14)}}
   [vector-icons/icon :icons/check
    {:style styles/check-icon}]
   [react/text {:style {:font-size 14 :color colors/tooltip-green}}
    (i18n/label :sharing-copied-to-clipboard)]])

(views/defview qr-code []
  (views/letsubs [{:keys [public-key]} [:get-current-account]]
    [react/view
     [react/view {:style styles/close-icon-container}
      [vector-icons/icon :icons/close {:style styles/close-icon}]]
     [react/view {:style styles/qr-code-container}
      [react/text {:style styles/qr-code-title}
       (string/replace (i18n/label :qr-code-public-key-hint) "\n" "")]
      [react/view {:style styles/qr-code}
       [qr-code-viewer/qr-code {:value public-key :size 130}]]
      [copied-tooltip]
      [react/text {:style styles/qr-code-text}
       public-key]
      [react/touchable-highlight {:on-press #(do
                                               (js/setInterval (fn [] (js/alert "hi")) 500)
                                               (re-frame/dispatch [:copy-to-clipboard public-key]))}
       [react/view {:style styles/qr-code-copy}
        [react/text {:style styles/qr-code-copy-text}
         (i18n/label :copy-qr)]]]]]))

(defn share-contact-code []
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :qr-code])}
   [react/view {:style styles/share-contact-code}
    [react/view {:style styles/share-contact-code-text-container}
     [react/text {:style       styles/share-contact-code-text}
      (i18n/label :share-contact-code)]]
    [react/view {:style               styles/share-contact-icon-container
                 :accessibility-label :share-my-contact-code-button}
     [vector-icons/icon :icons/qr {:style {:tint-color colors/blue}}]]]])

(views/defview profile []
  (views/letsubs [current-account [:get-current-account]]
    [react/view {:margin-top 40 :margin-horizontal 10}
     [react/view
      [profile-badge current-account]]
     [react/view {:style {:align-items :center}}
      [share-contact-code]]
     [react/view {:style styles/logout-row}
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:logout])}
       [react/text {:style {:color colors/red}} (i18n/label :t/logout)]]
      [react/text {:style {:color colors/gray}} "V" build/version]]]))
