(ns status-im.ui.screens.wallet.onboarding.setup.views
  (:require-macros [status-im.utils.views :as views])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.wallet.components :as comp]
            [status-im.ui.screens.wallet.onboarding.setup.styles :as styles]
            [status-im.ui.components.bottom-buttons.view :as bottom-buttons]
            [status-im.ui.components.button.view :as button]
            [status-im.utils.utils :as utils]))

(defn signing-word [word]
  [react/view styles/signing-word
   [react/text {:style           styles/signing-word-text
                :font            :roboto-mono
                :number-of-lines 1}
    word]])

(defn display-confirmation []
  (utils/show-question
    (i18n/label :t/wallet-set-up-confirm-title)
    (i18n/label :t/wallet-set-up-confirm-description)
    #(do (re-frame/dispatch [:wallet-set-up-passed])
         (re-frame/dispatch [:navigate-back]))))

(views/defview screen []
  (views/letsubs [{:keys [signing-phrase]} [:get-current-account]]
    (let [signing-words (string/split signing-phrase #" ")]
      [comp/simple-screen {:avoid-keyboard? true}
       [comp/toolbar (i18n/label :t/wallet-set-up-title)]
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
         [button/button {:on-press            display-confirmation
                         :text-style          styles/got-it-button-text
                         :accessibility-label :done-button}
          (i18n/label :t/got-it)
          nil]]]])))