(ns status-im.ui.screens.wallet.onboarding.setup.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.onboarding.setup.styles :as styles])
  (:require-macros [status-im.utils.views :as views]))

(defn signing-emoji [word first?]
  [react/view (merge styles/signing-emoji-container
                     (when-not first?
                       styles/signing-emoji-container-left-border))
   [react/text {:style           styles/signing-emoji
                :font            :roboto-mono
                :number-of-lines 1}
    word]])

(views/defview onboarding-panel [modal?]
  (views/letsubs [{:keys [signing-phrase]} [:get-current-account]]
    (let [signing-emojis (string/split signing-phrase #" ")
          container      (if modal? react/view wallet.components/simple-screen)
          container-opts (if modal? components.styles/flex {:avoid-keyboard? true})]
      [container container-opts
       [wallet.components/toolbar
        {}
        (actions/back-white #(re-frame/dispatch [:wallet-setup-navigate-back]))
        (i18n/label :t/wallet-setup-title)]
       [react/view {:style {:flex 1}}
        [react/view {:style {:flex 1
                             :flex-direction :column
                             :align-items :center
                             :justify-content :center}}
         [react/view {:style styles/signing-phrase}
          [signing-emoji (nth signing-emojis 0) true]
          [signing-emoji (nth signing-emojis 1) false]
          [signing-emoji (nth signing-emojis 2) false]]
         [react/text {:style styles/super-safe-transactions}
          (i18n/label :t/wallet-setup-safe-transactions)]
         [react/text {:style styles/description}
          (i18n/label :t/wallet-setup-description)]
         [react/view {:style styles/warning-container}
          [react/text {:style styles/warning}
           (i18n/label :t/wallet-setup-warning)]
          [vector-icons/icon :icons/info-warning styles/info-icon]]]
        [bottom-buttons/bottom-buttons styles/bottom-buttons
         nil
         [button/button {:on-press #(re-frame/dispatch [:wallet.setup.ui/got-it-button-pressed modal?])
                         :text-style styles/got-it-button-text
                         :accessibility-label :done-button}
          (i18n/label :t/got-it)
          nil]]]])))

(views/defview screen []
  [onboarding-panel false])

(views/defview modal []
  [react/view styles/modal
   [status-bar/status-bar {:type :modal-wallet}]
   [onboarding-panel true]])
