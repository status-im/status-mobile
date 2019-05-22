(ns status-im.ui.screens.wallet.onboarding.views
  (:require
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [status-im.i18n :as i18n]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.button.view :as button]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.react :as react]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.toolbar.actions :as actions]
   [status-im.ui.screens.wallet.components.views :as wallet.components]
   [status-im.ui.screens.wallet.onboarding.styles :as styles]
   [status-im.utils.utils :as utils])
  (:require-macros [status-im.utils.views :as views]))

(defn display-confirmation [on-accept]
  (utils/show-confirmation
   {:title   (i18n/label :t/wallet-set-up-confirm-title)
    :content (i18n/label :t/wallet-set-up-confirm-description)
    :cancel-button-text (i18n/label :t/see-it-again)
    :confirm-button-text (i18n/label :t/got-it)
    :on-accept on-accept}))

(defn info-bubble [text]
  ;; keeping styles inline here as we are going to
  ;; want to move this somewhere for reuse.
  [react/view {:style {:padding-top 10
                       :align-items :center}}
   [react/view {:style {:align-items :center
                        :position :absolute
                        :top 0
                        :width 34
                        :zIndex 1
                        :background-color colors/blue}}
    [vector-icons/icon :main-icons/info {:color colors/white}]]
   [react/view
    {:style {:border-color   (colors/alpha colors/white 0.6)
             :border-width   1
             :border-radius  8
             :padding-top    15
             :padding-bottom 15
             :padding-left   20
             :padding-right  20
             :align-items    :center}}
    [react/text
     {:style {:color      (colors/alpha colors/white 0.6)
              :text-align :center}}
     text]]])

(defn toolbar []
  ^{:key "toolbar"}
  [wallet.components/toolbar
   {:transparent? true}
   (actions/back-white #(re-frame/dispatch [:wallet.setup.ui/navigate-back-pressed]))
   (i18n/label :t/wallet-set-up-title)])

(defn main-panel [signing-phrase on-confirm]
  (let [signing-words (string/split signing-phrase #" ")]
    ^{:key "main-panel-view"}
    [react/view {:style styles/border-top-justify}
     [react/view] ;; crappy way to vertically center things
     [react/view {:style {:padding-left 36 :padding-right 36}}
      [react/view {:style styles/signing-phrase
                   :accessibility-label :signing-phrase}
       (map
        (fn [word container-style]
          ^{:key (str "signing-word-" word)}
          [react/view container-style
           [react/text {:style           styles/signing-word-text
                        :number-of-lines 1}
            word]])
        signing-words
        (cons
         (dissoc styles/signing-word
                 :border-left-color
                 :border-left-width)
         (repeat styles/signing-word))
        (cons true (repeat false)))]
      [react/view {:style styles/explanation-container}
       [react/text {:style styles/super-safe-text}
        (i18n/label :t/wallet-set-up-safe-transactions-title)]
       [react/text
        {:style styles/super-safe-explainer-text}
        (i18n/label :t/wallet-set-up-signing-explainer)]
       (info-bubble
        (i18n/label :t/wallet-set-up-signing-explainer-warning))]]
     [react/view {:style styles/bottom-button-container}
      [button/button {:on-press            on-confirm
                      :text-style          styles/got-it-button-text
                      :style {:padding-vertical 9}
                      :accessibility-label :done-button
                      :fit-to-text? false}
       (i18n/label :t/got-it)
       nil]]]))

(views/defview screen []
  (views/letsubs [{:keys [signing-phrase]} [:account/account]]
    [wallet.components/simple-screen
     {:avoid-keyboard? true}
     (toolbar)
     (main-panel
      signing-phrase
      (partial display-confirmation #(re-frame/dispatch [:accounts.ui/wallet-set-up-confirmed false])))]))

(views/defview modal []
  (views/letsubs [{:keys [signing-phrase]} [:account/account]]
    [react/view styles/modal
     [status-bar/status-bar {:type :modal-wallet}]
     [react/view components.styles/flex
      (toolbar)
      (main-panel
       signing-phrase
       (partial display-confirmation #(re-frame/dispatch [:accounts.ui/wallet-set-up-confirmed true])))]]))

(defn onboarding []
  [react/view styles/root
   [react/view {:style styles/onboarding-image-container}
    [react/image {:source (resources/get-image :wallet-welcome)
                  :style  styles/onboarding-image}]]
   [react/text {:style styles/onboarding-title}
    (i18n/label :t/wallet-onboarding-title)]
   [react/text {:style styles/onboarding-text}
    (i18n/label :t/wallet-onboarding-description)]
   [components.common/button
    {:button-style styles/set-up-button
     :label-style  styles/set-up-button-label
     :on-press     #(re-frame/dispatch [:navigate-to :wallet-onboarding-setup])
     :label        (i18n/label :t/wallet-onboarding-set-up)}]])
