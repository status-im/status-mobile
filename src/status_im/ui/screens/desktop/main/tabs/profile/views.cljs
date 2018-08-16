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
            [taoensso.timbre :as log]
            [clojure.string :as string]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.screens.desktop.main.tabs.profile.styles :as styles]
            [status-im.ui.screens.profile.user.views :as profile]))

(defn profile-badge [{:keys [name photo-path]}]
  [react/view styles/profile-badge
   [react/image {:source {:uri photo-path}
                 :style  styles/profile-photo}]
   [react/text {:style           styles/profile-user-name
                :number-of-lines 1}
    name]])

(views/defview copied-tooltip [opacity]
  (views/letsubs []
    [react/view {:style (styles/tooltip-container opacity)}
     [react/view {:style styles/tooltip-icon-text}
      [vector-icons/icon :icons/check
       {:style styles/check-icon}]
      [react/text {:style {:font-size 14 :color colors/tooltip-green-text}}
       (i18n/label :sharing-copied-to-clipboard)]]
     [react/view {:style styles/tooltip-triangle}]]))

(views/defview qr-code []
  (views/letsubs [{:keys [public-key]} [:get-current-account]
                  tooltip-opacity      [:get-in [:tooltips :qr-copied]]]
    [react/view
     [react/view {:style styles/qr-code-container}
      [react/text {:style styles/qr-code-title}
       (string/replace (i18n/label :qr-code-public-key-hint) "\n" "")]
      [react/view {:style styles/qr-code}
       [qr-code-viewer/qr-code {:value public-key :size 130}]]
      [react/view {:style {:align-items :center}}
       [react/text {:style            styles/qr-code-text
                    :selectable       true
                    :selection-color  colors/hawkes-blue}
        public-key]
       (when tooltip-opacity
         [copied-tooltip tooltip-opacity])]
      [react/touchable-highlight {:on-press #(do
                                               (re-frame/dispatch [:copy-to-clipboard public-key])
                                               (re-frame/dispatch [:show-tooltip :qr-copied]))}
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

(views/defview profile [user]
  [react/view styles/profile-view
   [profile-badge user]
   [share-contact-code]
   [react/view {:style styles/logout-row}
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:logout])}
     [react/text {:style (styles/logout-row-text colors/red)} (i18n/label :t/logout)]]
    [react/view [react/text {:style (styles/logout-row-text colors/gray)} "V" build/version " (" build/commit-sha ")"]]]])

(views/defview profile-data []
  (views/letsubs
    [user [:get-current-account]]
    [profile user]))
