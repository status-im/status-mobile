(ns status-im.ui.screens.wallet.onboarding.setup.views
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.onboarding.setup.styles :as styles]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.status-bar.view :as status-bar]))

(defn signing-word [word]
  [react/view styles/signing-word
   [react/text {:style           styles/signing-word-text
                :number-of-lines 1}
    word]])

(defn display-confirmation [modal?]
  (utils/show-question
   (i18n/label :t/wallet-set-up-confirm-title)
   (i18n/label :t/wallet-set-up-confirm-description)
   #(re-frame/dispatch [:accounts.ui/wallet-set-up-confirmed modal?])))

(views/defview onboarding-panel [modal?]
  (views/letsubs [{:keys [signing-phrase]} [:get-current-account]]
    (let [signing-words  (string/split signing-phrase #" ")
          container      (if modal? react/view wallet.components/simple-screen)
          container-opts (if modal? components.styles/flex {:avoid-keyboard? true})]
      [container container-opts
       [wallet.components/toolbar
        {}
        (actions/back-white #(re-frame/dispatch [:wallet-setup-navigate-back]))
        (i18n/label :t/wallet-set-up-title)]
       [react/view components.styles/flex
        [react/view {:style styles/setup-image-container}
         [react/image {:source (:wallet-setup resources/ui)
                       :style  styles/setup-image}]]
        [react/view {:style styles/signing-phrase}
         (for [word signing-words]
           ^{:key (str "signing-word-" word)}
           [signing-word word])]
        [react/text {:style styles/description}
         (i18n/label :t/wallet-set-up-signing-phrase)]
        [bottom-buttons/bottom-buttons styles/bottom-buttons
         nil
         [button/button {:on-press            (partial display-confirmation modal?)
                         :text-style          styles/got-it-button-text
                         :accessibility-label :done-button}
          (i18n/label :t/got-it)
          nil]]]])))

(views/defview screen []
  [onboarding-panel false])

(views/defview modal []
  [react/view styles/modal
   [status-bar/status-bar {:type :modal-wallet}]
   [onboarding-panel true]])
