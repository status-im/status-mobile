(ns status-im.ui.screens.multiaccounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as components.styles]
            [status-im.utils.config :as config]
            [status-im.ui.components.common.common :as components.common]
            [status-im.utils.security :as security]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.intro.views :as intro.views]
            [status-im.utils.utils :as utils]
            [status-im.constants :as constants]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.utils.platform :as platform]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.button :as button]))

(defview custom-seed-phrase []
  [react/view
   [react/view {:margin-top 24 :margin-horizontal 24 :align-items :center}
    [react/view {:width       32 :height 32 :border-radius 16
                 :align-items :center :justify-content :center}
     [icons/icon :main-icons/help {:color colors/blue}]]
    [react/text {:style {:typography    :title-bold
                         :margin-top    8
                         :margin-bottom 8}}
     (i18n/label :t/custom-seed-phrase)]
    [react/view {:flex-wrap       :wrap
                 :flex-direction  :row
                 :justify-content :center
                 :text-align      :center}
     [react/nested-text
      {:style {:color       colors/gray
               :text-align  :center
               :line-height 22}}
      (i18n/label :t/custom-seed-phrase-text-1)
      [{:style {:color colors/black}}
       (i18n/label :t/custom-seed-phrase-text-2)]
      (i18n/label :t/custom-seed-phrase-text-3)
      [{:style {:color colors/black}}
       (i18n/label :t/custom-seed-phrase-text-4)]]]
    [react/view {:margin-vertical 24
                 :align-items     :center}
     [button/button {:on-press            #(re-frame/dispatch [::multiaccounts.recover/continue-pressed])
                     :accessibility-label :continue-custom-seed-phrase
                     :label               (i18n/label :t/continue)}]
     [button/button {:on-press            #(re-frame/dispatch [:hide-popover])
                     :accessibility-label :cancel-custom-seed-phrase
                     :type                :secondary
                     :label               (i18n/label :t/cancel)}]]]])

(defn bottom-sheet-view []
  [react/view {:flex 1 :flex-direction :row}
   [react/view {:flex 1}
    [list-item/list-item
     {:theme               :action
      :title               :t/enter-seed-phrase
      :accessibility-label :enter-seed-phrase-button
      :icon                :main-icons/text
      :on-press            #(re-frame/dispatch [::multiaccounts.recover/enter-phrase-pressed])}]
    (when (and config/hardwallet-enabled?
               platform/android?)
      [list-item/list-item
       {:theme               :action
        :title               :t/recover-with-keycard
        :disabled?           (not config/hardwallet-enabled?)
        :accessibility-label :recover-with-keycard-button
        :icon                [react/view {:border-width     1
                                          :border-radius    20
                                          :border-color     colors/blue-light
                                          :background-color colors/blue-light
                                          :justify-content  :center
                                          :align-items      :center
                                          :width            40
                                          :height           40}
                              [react/image {:source (resources/get-image :keycard-logo-blue)
                                            :style  {:width 24 :height 24}}]]
        :on-press            #(re-frame/dispatch [::hardwallet/recover-with-keycard-pressed])}])]])

(def bottom-sheet
  {:content        bottom-sheet-view
   :content-height (if platform/android? 130 65)})

(defview enter-phrase []
  (letsubs [{:keys [processing?
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
          :error             (when passphrase-error (i18n/label passphrase-error))
          :placeholder       nil
          :height            120
          :multiline         true
          :auto-correct      false
          :keyboard-type     "visible-password"
          :container         {:background-color :white
                              :min-width        "50%"}
          :style             {:background-color :white
                              :text-align       :center
                              :font-size        16
                              :font-weight      "700"}}]]
       [react/view {:align-items :center}
        (when words-count
          [react/view {:flex-direction :row
                       :height         14
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
