(ns status-im.ui.screens.desktop.main.tabs.profile.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.utils.build :as build]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [clojure.string :as string]
            [status-im.ui.screens.offline-messaging-settings.views :as offline-messaging.views]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.screens.desktop.main.tabs.profile.styles :as styles]))

(defn profile-badge [{:keys [name photo-path]} editing?]
  [react/view styles/profile-badge
   [react/image {:source {:uri photo-path}
                 :style  styles/profile-photo}]
   (if editing?
     [react/text-input {:auto-focus    true
                        :default-value name
                        :on-change     (fn [e]
                                         (let [native-event (.-nativeEvent e)
                                               text         (.-text native-event)]
                                           (re-frame/dispatch [:my-profile/update-name text])))
                        :style         styles/profile-user-name}]
     [react/text {:style           styles/profile-user-name
                  :font            :medium
                  :number-of-lines 1}
      name])])

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
                  tooltip-opacity [:get-in [:tooltips :qr-copied]]]
    [react/view
     [react/view {:style styles/qr-code-container}
      [react/text {:style styles/qr-code-title
                   :font  :medium}
       (string/replace (i18n/label :qr-code-public-key-hint) "\n" "")]
      [react/view {:style styles/qr-code}
       [qr-code-viewer/qr-code {:value public-key :size 130}]]
      [react/view {:style {:align-items :center}}
       [react/text {:style           styles/qr-code-text
                    :selectable      true
                    :selection-color colors/hawkes-blue}
        public-key]
       (when tooltip-opacity
         [copied-tooltip tooltip-opacity])]
      [react/touchable-highlight {:on-press #(do
                                               (re-frame/dispatch [:copy-to-clipboard public-key])
                                               (re-frame/dispatch [:show-tooltip :qr-copied]))}
       [react/view {:style styles/qr-code-copy}
        [react/text {:style styles/qr-code-copy-text}
         (i18n/label :copy-qr)]]]]]))

(views/defview advanced-settings []
  (views/letsubs [current-wnode-id [:settings/current-wnode]
                  wnodes           [:settings/fleet-wnodes]]
    (let [render-fn (offline-messaging.views/render-row current-wnode-id)]
      [react/view
       [react/text {:style styles/advanced-settings-title
                    :font  :medium}
        (i18n/label :advanced-settings)]
       [react/view {:style styles/title-separator}]
       [react/text {:style styles/mailserver-title} (i18n/label :offline-messaging)]
       [react/view
        (for [node (vals wnodes)]
          ^{:key (:id node)}
          [react/view {:style {:margin-vertical 8}}
           [render-fn node]])]])))

(defn share-contact-code []
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :qr-code])}
   [react/view {:style styles/share-contact-code}
    [react/view {:style styles/share-contact-code-text-container}
     [react/text {:style styles/share-contact-code-text}
      (i18n/label :share-contact-code)]]
    [react/view {:style               styles/share-contact-icon-container
                 :accessibility-label :share-my-contact-code-button}
     [vector-icons/icon :icons/qr {:style {:tint-color colors/blue}}]]]])

(views/defview profile [user]
  (views/letsubs [current-view-id [:get :view-id]
                  editing?        [:get :my-profile/editing?]]
    (let [adv-settings-open? (= current-view-id :advanced-settings)]
      [react/view
       [react/view {:style styles/profile-edit}
        [react/touchable-highlight
         {:on-press #(if editing?
                       (re-frame/dispatch [:my-profile/save-profile])
                       (re-frame/dispatch [:my-profile/start-editing-profile]))}
         [react/text {:style {:color colors/blue}}
          (i18n/label (if editing? :t/done :t/edit))]]]
       [react/view styles/profile-view
        [profile-badge user editing?]
        [share-contact-code]
        [react/touchable-highlight {:style    (styles/profile-row adv-settings-open?)
                                    :on-press #(re-frame/dispatch [:navigate-to (if adv-settings-open? :home :advanced-settings)])}
         [react/view {:style styles/adv-settings}
          [react/text {:style (styles/profile-row-text colors/black)
                       :font  (if adv-settings-open? :medium :default)}
           (i18n/label :t/advanced-settings)]
          [vector-icons/icon :icons/forward {:style {:tint-color colors/gray}}]]]
        [react/view {:style (styles/profile-row false)}
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:logout])}
          [react/text {:style (styles/profile-row-text colors/red)} (i18n/label :t/logout)]]
         [react/view [react/text {:style (styles/profile-row-text colors/gray)} "V" build/version " (" build/commit-sha ")"]]]]])))

(views/defview profile-data []
  (views/letsubs [user [:get-current-account]]
    [profile user]))
