(ns status-im.ui.screens.multiaccounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]
                    :as views])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.multiaccounts.recover.styles :as styles]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.config :as config]
            [status-im.utils.core :as utils.core]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.components.common.common :as components.common]
            [status-im.utils.security :as security]
            [status-im.utils.platform :as platform]
            [clojure.string :as string]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon]
            [status-im.ui.components.radio :as radio]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.intro.views :as intro.views]
            [status-im.utils.utils :as utils]
            [status-im.constants :as constants]))

(defview passphrase-input [passphrase error warning]
  (letsubs [input-ref (reagent/atom nil)]
    [text-input/text-input-with-label
     {:style               styles/recovery-phrase-input
      :height              92
      :ref                 (partial reset! input-ref)
      :label               (i18n/label :t/recovery-phrase)
      :accessibility-label :enter-12-words
      :placeholder         (i18n/label :t/enter-12-words)
      :multiline           true
      :default-value       passphrase
      :auto-correct        false
      :on-change-text      #(re-frame/dispatch [::multiaccounts.recover/passphrase-input-changed (security/mask-data %)])
      :on-blur             #(re-frame/dispatch [::multiaccounts.recover/passphrase-input-blured])
      :error               (cond error (i18n/label error)
                                 warning (i18n/label warning))}]))

(defview password-input [password error on-submit-editing]
  (views/letsubs [inp-ref (atom nil)]
    {:component-will-update
     (fn [_ [_ new-password]]
       (when (and (string? new-password)
                  (string/blank? new-password)
                  @inp-ref)
         (.clear @inp-ref)))}
    [react/view {:style                       styles/password-input
                 :important-for-accessibility :no-hide-descendants}
     [text-input/text-input-with-label
      {:label               (i18n/label :t/password)
       :accessibility-label :enter-password
       :placeholder         (i18n/label :t/enter-password)
       :default-value       password
       :auto-focus          false
       :on-change-text      #(re-frame/dispatch [::multiaccounts.recover/password-input-changed (security/mask-data %)])
       :on-blur             #(re-frame/dispatch [::multiaccounts.recover/password-input-blured])
       :secure-text-entry   true
       :error               (when error (i18n/label error))
       :on-submit-editing   on-submit-editing
       :ref                 #(reset! inp-ref %)}]]))

(defview recover []
  (letsubs [recovered-multiaccount [:get-recover-multiaccount]]
    (let [{:keys [passphrase password passphrase-valid? password-valid?
                  password-error passphrase-error passphrase-warning processing?]} recovered-multiaccount
          valid-form? (and password-valid? passphrase-valid?)
          disabled?   (or (not recovered-multiaccount)
                          processing?
                          (not valid-form?))
          sign-in     #(re-frame/dispatch [::multiaccounts.recover/sign-in-button-pressed])]
      [react/keyboard-avoiding-view {:style styles/screen-container}
       [status-bar/status-bar]
       [toolbar/toolbar nil toolbar/default-nav-back
        [toolbar/content-title (i18n/label :t/sign-in-to-another)]]
       [react/view styles/inputs-container
        [passphrase-input (or passphrase "") passphrase-error passphrase-warning]
        [password-input (or password "") password-error (when-not disabled? sign-in)]
        (when platform/desktop?
          [react/i18n-text {:style styles/recover-release-warning
                            :key   :recover-multiaccount-warning}])]
       [react/view components.styles/flex]
       (if processing?
         [react/view styles/processing-view
          [react/activity-indicator {:animating true}]
          [react/i18n-text {:style styles/sign-you-in
                            :key   :sign-you-in}]]
         [react/view {:style styles/bottom-button-container}
          [react/view {:style components.styles/flex}]
          [components.common/bottom-button
           {:forward?  true
            :label     (i18n/label :t/sign-in)
            :disabled? disabled?
            :on-press  sign-in}]])])))

(defn bottom-sheet-view []
  [react/view {:flex 1 :flex-direction :row}
   [react/view action-button.styles/actions-list
    [action-button/action-button
     {:label               (i18n/label :t/enter-seed-phrase)
      :accessibility-label :enter-seed-phrase-button
      :icon                :main-icons/text
      :icon-opts           {:color colors/blue}
      :on-press            #(re-frame/dispatch [::multiaccounts.recover/enter-phrase-pressed])}]
    [action-button/action-button
     {:label               (i18n/label :t/recover-with-keycard)
      :label-style         (if config/hardwallet-enabled? {} {:color colors/gray})
      :accessibility-label :recover-with-keycard-button
      :image               :keycard-logo-blue
      :image-opts          {:style {:width 24 :height 24}}
      :on-press            #(when config/hardwallet-enabled?
                              (re-frame/dispatch [::hardwallet/recover-with-keycard-pressed]))}]]])

(def bottom-sheet
  {:content        bottom-sheet-view
   :content-height 130})

(defview enter-phrase []
  (letsubs [{:keys [passphrase
                    processing?
                    passphrase-error
                    words-count
                    next-button-disabled?]} [:get-recover-multiaccount]]
    [react/keyboard-avoiding-view {:flex             1
                                   :justify-content  :space-between
                                   :background-color colors/white}
     [toolbar/toolbar
      {:transparent? true
       :style        {:margin-top 32}}
      [toolbar/nav-text
       {:handler #(re-frame/dispatch [::multiaccounts.recover/cancel-pressed])
        :style   {:padding-left 21}}
       (i18n/label :t/cancel)]
      [react/text {:style {:color colors/gray}}
       (i18n/label :t/step-i-of-n {:step   "1"
                                   :number "2"})]]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/multiaccounts-recover-enter-phrase-title)]]
       [react/view {:margin-top 16}
        [text-input/text-input-with-label
         {:on-change-text    #(re-frame/dispatch [::multiaccounts.recover/enter-phrase-input-changed (security/mask-data %)])
          :auto-focus        true
          :on-submit-editing #(re-frame/dispatch [::multiaccounts.recover/enter-phrase-input-submitted])
          :error             (when passphrase-error (i18n/label passphrase-error))
          :placeholder       nil
          :height            120
          :multiline         true
          :auto-correct      false
          :container         {:background-color :white
                              :min-width        "50%"}
          :style             {:background-color :white
                              :text-align       :center
                              :font-size        16
                              :font-weight      "700"}}]]
       [react/view {:align-items :center}
        (when words-count
          [react/view {:flex-direction :row
                       :height         11
                       :align-items    :center}
           (when-not next-button-disabled?
             [vector-icons/tiny-icon :tiny-icons/tiny-check])
           [react/text {:style {:font-size    14
                                :padding-left 4
                                :text-align   :center
                                :color        colors/black}}
            (i18n/label-pluralize words-count :t/words-n)]])]
       (when next-button-disabled?
         [react/view {:margin-top  17
                      :align-items :center}
          [react/text {:style {:color      colors/black
                               :font-size  14
                               :text-align :center}}
           (i18n/label :t/multiaccounts-recover-enter-phrase-text)]])]
      (when processing?
        [react/view
         [react/activity-indicator {:size      :large
                                    :animating true}]
         [react/text {:style {:color      colors/gray
                              :margin-top 8}}
          (i18n/label :t/processing)]])
      [react/view {:flex-direction  :row
                   :justify-content :space-between
                   :align-items     :center
                   :width           "100%"
                   :height          86}
       (when-not processing?
         [react/view])
       (when-not processing?
         [react/view {:margin-right 20}
          [components.common/bottom-button
           {:on-press  #(re-frame/dispatch [::multiaccounts.recover/enter-phrase-next-pressed])
            :label     (i18n/label :t/next)
            :disabled? next-button-disabled?
            :forward?  true}]])]]]))

(defview success []
  (letsubs [multiaccount [:get-recover-multiaccount]]
    (let [pubkey (get-in multiaccount [:derived constants/path-whisper-keyword :publicKey])]
      [react/view {:flex             1
                   :justify-content  :space-between
                   :background-color colors/white}
       [toolbar/toolbar
        {:transparent? true
         :style        {:margin-top 32}}
        nil
        nil]
       [react/view {:flex            1
                    :flex-direction  :column
                    :justify-content :space-between
                    :align-items     :center}
        [react/view {:flex-direction :column
                     :align-items    :center}
         [react/view {:margin-top 16}
          [react/text {:style {:typography :header
                               :text-align :center}}
           (i18n/label :t/keycard-recovery-success-header)]]
         [react/view {:margin-top  16
                      :width       "85%"
                      :align-items :center}
          [react/text {:style {:color      colors/gray
                               :text-align :center}}
           (i18n/label :t/recovery-success-text)]]]
        [react/view {:flex-direction  :column
                     :flex            1
                     :justify-content :center
                     :align-items     :center}
         [react/view {:margin-horizontal 16
                      :flex-direction    :column}
          [react/view {:justify-content :center
                       :align-items     :center
                       :margin-bottom   11}
           [react/image {:source {:uri (identicon/identicon pubkey)}
                         :style  {:width         61
                                  :height        61
                                  :border-radius 30
                                  :border-width  1
                                  :border-color  colors/black-transparent}}]]
          [react/text {:style           {:text-align  :center
                                         :color       colors/black
                                         :font-weight "500"}
                       :number-of-lines 1
                       :ellipsize-mode  :middle}
           (gfy/generate-gfy pubkey)]
          [react/text {:style           {:text-align  :center
                                         :margin-top  4
                                         :color       colors/gray
                                         :font-family "monospace"}
                       :number-of-lines 1
                       :ellipsize-mode  :middle}
           (utils/get-shortened-address pubkey)]]]
        [react/view {:margin-bottom 50}
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [::multiaccounts.recover/re-encrypt-pressed])}
          [react/view {:background-color colors/blue-light
                       :align-items      :center
                       :justify-content  :center
                       :flex-direction   :row
                       :width            193
                       :height           44
                       :border-radius    10}
           [react/text {:style {:color colors/blue}}
            (i18n/label :t/re-encrypt-key)]]]]]])))

(defview select-storage []
  (letsubs [{:keys [selected-storage-type]} [:intro-wizard]
            {view-height :height} [:dimensions/window]]
    [react/view {:flex             1
                 :justify-content  :space-between
                 :background-color colors/white}
     [toolbar/toolbar
      {:transparent? true
       :style        {:margin-top 32}}
      [toolbar/nav-text
       {:handler #(re-frame/dispatch [::multiaccounts.recover/cancel-pressed])
        :style   {:padding-left 21}}
       (i18n/label :t/cancel)]
      nil]
     [react/view {:flex            1
                  :justify-content :space-between}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/intro-wizard-title3)]]
       [react/view {:margin-top  16
                    :width       "85%"
                    :align-items :center}
        [react/text {:style {:color      colors/gray
                             :text-align :center}}
         (i18n/label :t/intro-wizard-text3)]]]
      [intro.views/select-key-storage {:selected-storage-type (if config/hardwallet-enabled? selected-storage-type :default)} view-height]
      [react/view {:flex-direction  :row
                   :justify-content :space-between
                   :align-items     :center
                   :width           "100%"
                   :height          86}
       [react/view components.styles/flex]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press #(re-frame/dispatch [::multiaccounts.recover/select-storage-next-pressed])
          :forward? true}]]]]]))

(defview enter-password []
  (letsubs [{:keys [password password-error]} [:get-recover-multiaccount]]
    [react/keyboard-avoiding-view {:flex             1
                                   :justify-content  :space-between
                                   :background-color colors/white}
     [toolbar/toolbar
      {:transparent? true
       :style        {:margin-top 32}}
      [toolbar/nav-text
       {:handler #(re-frame/dispatch [::multiaccounts.recover/cancel-pressed])
        :style   {:padding-left 21}}
       (i18n/label :t/cancel)]
      [react/text {:style {:color colors/gray}}
       (i18n/label :t/step-i-of-n {:step   "1"
                                   :number "2"})]]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/intro-wizard-title-alt4)]]
       [react/view {:margin-top  16
                    :width       "85%"
                    :align-items :center}
        [react/text {:style {:color      colors/gray
                             :text-align :center}}
         (i18n/label :t/password-description)]]
       [react/view {:margin-top 16}
        [text-input/text-input-with-label
         {:on-change-text    #(re-frame/dispatch [::multiaccounts.recover/enter-password-input-changed (security/mask-data %)])
          :auto-focus        true
          :on-submit-editing #(re-frame/dispatch [::multiaccounts.recover/enter-password-input-submitted])
          :secure-text-entry true
          :error             (when password-error (i18n/label password-error))
          :placeholder       nil
          :height            125
          :multiline         false
          :auto-correct      false
          :container         {:background-color :white
                              :min-width        "50%"}
          :style             {:background-color :white
                              :width            200
                              :text-align       :center
                              :font-size        20
                              :font-weight      "700"}}]]]
      [react/view {:flex-direction  :row
                   :justify-content :space-between
                   :align-items     :center
                   :width           "100%"
                   :height          86}
       [react/view]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press  #(re-frame/dispatch [::multiaccounts.recover/enter-password-next-pressed])
          :label     (i18n/label :t/next)
          :disabled? (empty? password)
          :forward?  true}]]]]]))

(defview confirm-password []
  (letsubs [{:keys [password-confirmation password-error]} [:get-recover-multiaccount]]
    [react/keyboard-avoiding-view {:flex             1
                                   :justify-content  :space-between
                                   :background-color colors/white}
     [toolbar/toolbar
      {:transparent? true
       :style        {:margin-top 32}}
      [toolbar/nav-text
       {:handler #(re-frame/dispatch [::multiaccounts.recover/cancel-pressed])
        :style   {:padding-left 21}}
       (i18n/label :t/cancel)]
      [react/text {:style {:color colors/gray}}
       (i18n/label :t/step-i-of-n {:step   "1"
                                   :number "2"})]]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/intro-wizard-title-alt5)]]
       [react/view {:margin-top  16
                    :width       "85%"
                    :align-items :center}
        [react/text {:style {:color      colors/gray
                             :text-align :center}}
         (i18n/label :t/password-description)]]
       [react/view {:margin-top 16}
        [text-input/text-input-with-label
         {:on-change-text    #(re-frame/dispatch [::multiaccounts.recover/confirm-password-input-changed %])
          :auto-focus        true
          :on-submit-editing #(re-frame/dispatch [::multiaccounts.recover/confirm-password-input-submitted])
          :error             (when password-error (i18n/label password-error))
          :secure-text-entry true
          :placeholder       nil
          :height            125
          :multiline         false
          :auto-correct      false
          :container         {:background-color :white
                              :min-width        "50%"}
          :style             {:background-color :white
                              :width            200
                              :text-align       :center
                              :font-size        20
                              :font-weight      "700"}}]]]
      [react/view {:flex-direction  :row
                   :justify-content :space-between
                   :align-items     :center
                   :width           "100%"
                   :height          86}
       [react/view]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press  #(re-frame/dispatch [::multiaccounts.recover/confirm-password-next-pressed])
          :label     (i18n/label :t/next)
          :disabled? (empty? password-confirmation)
          :forward?  true}]]]]]))
