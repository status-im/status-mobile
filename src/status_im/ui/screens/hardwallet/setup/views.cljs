(ns status-im.ui.screens.hardwallet.setup.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [goog.functions :refer [debounce]]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.screens.profile.seed.views :as seed.views]
            [status-im.ui.screens.hardwallet.components :as components]
            [status-im.ui.screens.hardwallet.pin.views :as pin.views]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.common.common :as components.common]
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.hardwallet.setup.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.hardwallet.setup.styles :as styles]
            [status-im.utils.security :as security]))

(defn event-emitter [] (.-DeviceEventEmitter js-dependencies/react-native))

(defview secret-keys []
  (letsubs [secrets [:hardwallet-secrets]]
    [react/view styles/secret-keys-container
     [react/scroll-view
      [react/view styles/secret-keys-inner-container
       [react/view
        [react/image {:source (resources/get-image :secret-keys)
                      :style  styles/secret-keys-image-container}]]
       [react/view styles/secret-keys-title-container
        [react/text {:style           styles/secret-keys-title-text
                     :number-of-lines 2}
         (i18n/label :t/write-down-and-store-securely)]]
       [react/view styles/puk-code-numbers-container
        [react/view styles/puk-code-numbers-inner-container
         [react/text {:style styles/puk-code-title-text}
          (i18n/label :t/pin-code)]
         [react/view styles/puk-code-numbers-border-container]
         [react/text {:style styles/puk-code-text}
          (:pin secrets)]]]
       [react/view styles/secret-code-explanation-container
        [react/text {:style styles/puk-code-explanation-text}
         (i18n/label :t/pin-code-description)]]
       [react/view styles/puk-code-numbers-container
        [react/view styles/puk-code-numbers-inner-container
         [react/text {:style styles/puk-code-title-text}
          (i18n/label :t/puk-code)]
         [react/view styles/puk-code-numbers-border-container]
         [react/text {:style styles/puk-code-text}
          (:puk secrets)]]]
       [react/view styles/secret-code-explanation-container
        [react/text {:style styles/puk-code-explanation-text}
         (i18n/label :t/puk-code-explanation)]]
       [react/view styles/puk-code-numbers-container
        [react/view styles/puk-code-numbers-inner-container
         [react/text {:style styles/puk-code-title-text}
          (i18n/label :t/pair-code)]
         [react/view styles/puk-code-numbers-border-container]
         [react/text {:style styles/puk-code-text}
          (:password secrets)]]]
       [react/view styles/secret-code-explanation-container
        [react/text {:style           styles/puk-code-explanation-text
                     :number-of-lines 2}
         (i18n/label :t/pair-code-explanation)]]]
      [react/view styles/next-button-container
       [react/view components.styles/flex]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press #(re-frame/dispatch [:hardwallet.ui/secret-keys-next-button-pressed])
          :forward? true}]]]]]))

(defview card-ready []
  (letsubs [flow [:hardwallet-flow]]
    [react/view styles/card-ready-container
     [react/view styles/card-ready-inner-container
      [react/view (assoc styles/center-container :margin-top 68)
       [react/text {:style styles/center-title-text}
        (i18n/label :t/card-is-paired)]
       [react/text {:style styles/estimated-time-text}
        (if (= flow :create)
          (i18n/label :t/next-step-generating-mnemonic)
          (i18n/label :t/next-step-entering-mnemonic))]]
      [react/view]
      [react/view styles/next-button-container
       [react/view components.styles/flex]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press #(re-frame/dispatch [:hardwallet.ui/card-ready-next-button-pressed])
          :forward? true}]]]]]))

(defn- import-multiaccount []
  [react/view styles/card-ready-container
   [react/view styles/card-ready-inner-container
    [react/view (assoc styles/center-container :margin-top 68)
     [react/text {:style styles/center-title-text}
      (i18n/label :t/card-is-paired)]
     [react/text {:style styles/estimated-time-text}
      (i18n/label :t/ready-to-import-keycard-multiaccount)]]
    [react/view]]
   [react/view styles/back-and-next-buttons-container
    [components.common/bottom-button
     {:on-press #(re-frame/dispatch [:hardwallet.ui/import-multiaccount-back-button-pressed])
      :back?    true
      :label    (i18n/label :t/back)}]
    [components.common/bottom-button
     {:on-press #(re-frame/dispatch [:hardwallet.ui/import-multiaccount-next-button-pressed])
      :forward? true}]]])

(defview display-recovery-phrase []
  (letsubs [mnemonic [:hardwallet-mnemonic]]
    (let [mnemonic-vec (vec (map-indexed vector (clojure.string/split mnemonic #" ")))]
      [react/view styles/card-ready-container
       [react/view styles/recovery-phrase-inner-container
        [react/view styles/center-container
         [react/text {:style           styles/center-title-text
                      :number-of-lines 2}
          (i18n/label :t/your-recovery-phrase)]
         [react/view
          [react/view {:style {:margin-top        17
                               :margin-bottom     16
                               :margin-horizontal 16
                               :flex-direction    :row
                               :border-radius     8
                               :background-color  colors/white
                               :border-width      1
                               :border-color      colors/gray-lighter}}
           [seed.views/six-words (subvec mnemonic-vec 0 6)]
           [react/view {:style {:width            1
                                :background-color colors/gray-lighter}}]
           [seed.views/six-words (subvec mnemonic-vec 6 12)]]
          [react/view styles/recovery-phrase-description
           [react/text (i18n/label :t/your-recovery-phrase-description)]]]]]
       [react/view styles/next-button-container
        [react/view components.styles/flex]
        [react/view {:margin-right 20}
         [components.common/bottom-button
          {:on-press #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-next-button-pressed])
           :label    (i18n/label :t/next)
           :forward? true}]]]])))

(defview confirm-word-input [error ref step]
  {:component-will-update #(.clear @ref)}
  [text-input/text-input-with-label
   {:on-change-text    #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-confirm-word-input-changed %])
    :auto-focus        true
    :ref               (partial reset! ref)
    :on-submit-editing #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-confirm-word-next-button-pressed])
    :error             error
    :placeholder       (i18n/label :t/enter-word)}])

(defview recovery-phrase-confirm-word [step]
  ^{:key (str step)}
  (letsubs [width [:dimensions/window-width]
            word [:hardwallet-recovery-phrase-word]
            input-word [:hardwallet-recovery-phrase-input-word]
            error [:hardwallet-recovery-phrase-confirm-error]
            ref (atom nil)]
    (let [{:keys [word idx]} word]
      [react/view styles/enter-pair-code-container
       [react/view styles/enter-pair-code-title-container
        [react/view
         [react/text {:style styles/check-recovery-phrase-text}
          (i18n/label :t/check-your-recovery-phrase)]
         [react/text {:style styles/recovery-phrase-word-n-text}
          (i18n/label :t/word-n {:number (inc idx)})]]
        [react/view (styles/enter-pair-code-input-container width)
         [confirm-word-input error ref step]]]
       [react/view styles/back-and-next-buttons-container
        [components.common/bottom-button
         {:on-press #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-confirm-word-back-button-pressed])
          :back?    true
          :label    (i18n/label :t/back)}]
        [components.common/bottom-button
         {:on-press  #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-confirm-word-next-button-pressed])
          :disabled? (empty? input-word)
          :forward?  true}]]])))

(defview enter-recovery-phrase []
  (letsubs [width [:dimensions/window-width]
            recovered-multiaccount [:get-recover-multiaccount]]
    (let [{:keys [passphrase passphrase-valid? passphrase-error passphrase-warning]} recovered-multiaccount
          disabled? (not passphrase-valid?)
          validate-passphrase (debounce
                               #(re-frame/dispatch [:multiaccounts.recover.ui/passphrase-input-blured])
                               1000)]
      [react/view styles/enter-pair-code-container
       [react/view styles/enter-pair-code-title-container
        [react/view
         [react/text {:style           styles/center-title-text
                      :number-of-lines 2}
          (i18n/label :t/your-recovery-phrase)]
         [react/view (styles/enter-pair-code-input-container width)
          [text-input/text-input-with-label
           {:style               {:flex                1
                                  :text-align-vertical :top}
            :height              92
            :accessibility-label :enter-12-words
            :placeholder         (i18n/label :t/enter-12-words)
            :multiline           true
            :default-value       passphrase
            :auto-correct        false
            :on-change-text      #(do
                                    (re-frame/dispatch [:multiaccounts.recover.ui/passphrase-input-changed (security/mask-data %)])
                                    (validate-passphrase))
            :error               (cond passphrase-error (i18n/label passphrase-error)
                                       passphrase-warning (i18n/label passphrase-warning))}]]]]
       [react/view styles/next-button-container
        [react/view components.styles/flex]
        [react/view {:margin-right 20}
         [components.common/bottom-button
          {:on-press  #(re-frame/dispatch [:hardwallet.ui/recovery-phrase-next-button-pressed])
           :label     (i18n/label :t/next)
           :disabled? disabled?
           :forward?  true}]]]])))

(defview recovery-phrase []
  (letsubs [flow [:hardwallet-flow]]
    (if (= flow :create)
      (display-recovery-phrase)
      (enter-recovery-phrase))))

(defview enter-pair-code-input [ref]
  {:component-will-update #(.clear @ref)}
  [text-input/text-input-with-label
   {:on-change-text #(re-frame/dispatch [:hardwallet.ui/pair-code-input-changed %])
    :auto-focus     true
    :ref            (partial reset! ref)
    :placeholder    ""}])

(defview enter-pair-code []
  (letsubs [pair-code [:hardwallet-pair-code]
            error [:hardwallet-setup-error]
            width [:dimensions/window-width]
            ref (atom nil)]
    [react/scroll-view
     [react/view styles/enter-pair-code-container
      [react/view styles/enter-pair-code-title-container
       [react/view
        [react/text {:style styles/enter-pair-code-title-text}
         (i18n/label :t/enter-pair-code)]
        [react/text {:style styles/enter-pair-code-explanation-text}
         (i18n/label :t/enter-pair-code-description)]]
       (when error
         [react/view
          [react/text {:style {:font-weight "700"
                               :padding-top 10
                               :color       colors/red}}
           error]])
       [react/view (styles/enter-pair-code-input-container width)
        [enter-pair-code-input ref]]]
      [react/view styles/next-button-container
       [react/view components.styles/flex]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press  #(re-frame/dispatch [:hardwallet.ui/pair-code-next-button-pressed])
          :disabled? (empty? pair-code)
          :forward?  true}]]]]]))

(defn- card-with-button-view
  [{:keys [text-label button-label button-container-style on-press show-icon?]}]
  "Generic view with centered card image and button at the bottom.
  Used by 'Prepare', 'Pair', 'No slots', 'Card is linked' screens"
  [react/view styles/card-with-button-view-container
   [react/view styles/hardwallet-card-image-container
    [react/image {:source (resources/get-image :hardwallet-card)
                  :style  styles/hardwallet-card-image}]
    [react/view styles/center-text-container
     [react/text {:style (assoc styles/center-text :padding-horizontal 60)}
      (i18n/label text-label)]]]
   [react/view styles/bottom-container
    [react/touchable-highlight
     {:on-press on-press}
     [react/view (merge styles/bottom-button-container button-container-style)
      [react/text {:style styles/bottom-button-text}
       (i18n/label button-label)]
      (when show-icon?
        [vector-icons/icon :icons/link {:color           colors/blue
                                        :container-style {:margin-left 5}}])]]]])

(defn begin []
  [react/view styles/card-blank-container
   [react/scroll-view
    [react/view styles/hardwallet-card-image-container
     [react/text {:style styles/card-is-empty-text}
      (i18n/label :t/card-is-blank)]
     [react/view {:margin-top 15}
      [react/image {:source (resources/get-image :hardwallet-card)
                    :style  styles/hardwallet-card-image}]]
     [react/view styles/card-is-empty-prepare-text
      [react/text {:style styles/center-text}
       (i18n/label :t/card-setup-prepare-text)]]]
    [react/view styles/remaining-steps-container
     [react/text {:style styles/remaining-steps-text}
      (i18n/label :t/remaining-steps)]
     [react/view {:margin-bottom 25}
      (for [[number text] [["1" (i18n/label :t/create-pin)]
                           ["2" (i18n/label :t/initialization-of-the-card)]
                           ["3" (i18n/label :t/puk-and-pairing-codes-displayed)]
                           ["4" (i18n/label :t/device-pairing)]
                           ["5" (i18n/label :t/recovery-phrase)]]]
        ^{:key number} [react/view styles/remaining-step-row
                        [react/view styles/remaining-step-row-text
                         [react/text number]]
                        [react/view styles/remaining-step-row-text2
                         [react/text text]]])]]
    [react/view styles/bottom-container
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:hardwallet.ui/begin-setup-button-pressed])}
      [react/view styles/begin-button-container
       [react/text {:style styles/bottom-button-text}
        (i18n/label :t/begin-set-up)]]]]]])

(defn pair []
  [card-with-button-view {:text-label   :t/pair-card-question
                          :button-label :t/pair-card
                          :on-press     #(re-frame/dispatch [:hardwallet.ui/pair-card-button-pressed])}])

(defn no-slots []
  [card-with-button-view {:text-label             :t/no-pairing-slots-available
                          :button-label           :t/help-capitalized
                          :show-icon?             true
                          :button-container-style {:background-color colors/white}
                          :on-press               #(.openURL (react/linking) "https://hardwallet.status.im")}])

(defn card-already-linked []
  [card-with-button-view {:text-label             :t/card-already-linked
                          :button-label           :t/help-capitalized
                          :show-icon?             true
                          :button-container-style {:background-color colors/white}
                          :on-press               #(.openURL (react/linking) "https://hardwallet.status.im")}])

(defview error []
  (letsubs [error [:hardwallet-setup-error]]
    [react/view styles/card-with-button-view-container
     [react/view styles/hardwallet-card-image-container
      [react/image {:source (resources/get-image :hardwallet-card)
                    :style  styles/hardwallet-card-image}]
      [react/view styles/center-text-container
       [react/text {:style styles/center-text}
        (i18n/label :t/something-went-wrong)]
       [react/view {:padding-horizontal 20
                    :margin-top         20}
        [react/text {:style styles/center-text}
         (if (map? error)
           (str (:code error) "\n" (:error error))
           (str error))]]]]
     [react/view styles/bottom-container
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:hardwallet.ui/error-button-pressed])}
       [react/view styles/bottom-button-container
        [react/text {:style styles/bottom-button-text}
         (i18n/label :t/try-again)]]]]]))

(defn- loading-view [{:keys [title-label text-label estimated-time-seconds step-number]}]
  "Generic view with waiting time estimate and loading indicator.
  Used by 'Prepare', 'Pairing', 'Completing' screens"
  [react/view styles/loading-view-container
   [react/view styles/center-container
    [react/text {:style styles/center-title-text}
     (i18n/label title-label)]
    (when text-label
      [react/text {:style           styles/generating-codes-for-pairing-text
                   :number-of-lines 2}
       (i18n/label text-label)])
    [react/text {:style styles/estimated-time-text}
     (i18n/label :t/this-will-take-few-seconds)]]
   [react/view styles/waiting-indicator-container
    [react/activity-indicator {:animating true
                               :size      :large}]]])

(defview preparing []
  (letsubs [progress-bar (atom nil)
            listener (atom nil)
            card-state [:hardwallet-card-state]]
    {:component-did-mount (fn []
                            (when @listener
                              (.removeListener @listener))
                            (reset! listener
                                    (.addListener (event-emitter)
                                                  "keycardInstallationProgress"
                                                  (fn [params]
                                                    (when @progress-bar
                                                      (.setNativeProps @progress-bar params))))))}
    [react/view styles/loading-view-container
     [react/view styles/center-container
      [react/text {:style styles/center-title-text}
       (i18n/label :t/preparing-card)]
      [react/text {:style           styles/generating-codes-for-pairing-text
                   :number-of-lines 2}
       (i18n/label :t/generating-codes-for-pairing)]
      [react/text {:style styles/estimated-time-text}
       (i18n/label :t/taking-long-hold-phone-connected)]]
     [react/view styles/progress-bar-container
      (if (contains? #{:blank :init} card-state)
        [(react/progress-bar) {:styleAttr     "Horizontal"
                               :indeterminate false
                               :progress      0
                               :ref           #(reset! progress-bar %)}]
        [react/activity-indicator {:animating true
                                   :size      :large}])]]))

(defn- generating-mnemonic []
  [react/view styles/loading-view-container
   [react/view styles/center-container
    [react/text {:style styles/center-title-text}
     (i18n/label :t/generating-mnemonic)]
    [react/text {:style styles/estimated-time-text}
     (i18n/label :t/this-will-take-few-seconds)]]
   [react/view styles/waiting-indicator-container
    [react/activity-indicator {:animating true
                               :size      :large}]]])

(defn- loading-keys []
  [react/view styles/loading-view-container
   [react/view styles/center-container
    [react/text {:style styles/center-title-text}
     (i18n/label :t/finishing-card-setup)]
    [react/text {:style           styles/generating-codes-for-pairing-text
                 :number-of-lines 2}
     (i18n/label :t/finishing-card-setup-steps)]
    [react/text {:style styles/estimated-time-text}
     (i18n/label :t/this-will-take-few-seconds)]]
   [react/view styles/waiting-indicator-container
    [react/activity-indicator {:animating true
                               :size      :large}]]])

(defn- pairing []
  [react/view styles/loading-view-container
   [react/view styles/center-container
    [react/text {:style styles/center-title-text}
     (i18n/label :t/pairing-card)]
    [react/text {:style styles/estimated-time-text}
     (i18n/label :t/this-will-take-few-seconds)]]
   [react/view styles/waiting-indicator-container
    [react/activity-indicator {:animating true
                               :size      :large}]]])

(defn complete []
  [loading-view {:title-label            :t/completing-card-setup
                 :estimated-time-seconds 30
                 :step-number            3}])

(defn- importing-multiaccount []
  [react/view styles/loading-view-container
   [react/view styles/center-container
    [react/text {:style styles/center-title-text}
     (i18n/label :t/importing-keycard-multiaccount)]
    [react/text {:style styles/estimated-time-text}
     (i18n/label :t/this-will-take-few-seconds)]]
   [react/view styles/waiting-indicator-container
    [react/activity-indicator {:animating true
                               :size      :large}]]])

(defn- content [step]
  (case step
    :begin [begin]
    :preparing [preparing]
    :secret-keys [secret-keys]
    :card-ready [card-ready]
    :complete [complete]
    :pair [pair]
    :generating-mnemonic [generating-mnemonic]
    :loading-keys [loading-keys]
    :enter-pair-code [enter-pair-code]
    :no-slots [no-slots]
    :card-already-linked [card-already-linked]
    :pairing [pairing]
    :pin [pin.views/create-pin]
    :recovery-phrase [recovery-phrase]
    :recovery-phrase-confirm-word1 [recovery-phrase-confirm-word step]
    :recovery-phrase-confirm-word2 [recovery-phrase-confirm-word step]
    :error [error]
    :import-multiaccount [import-multiaccount]
    :importing-multiaccount [importing-multiaccount]
    [begin]))

(defview hardwallet-setup []
  (letsubs [step [:hardwallet-setup-step]]
    [react/keyboard-avoiding-view components.styles/flex
     [react/view styles/container
      [react/view styles/inner-container
       [components/maintain-card step]]
      [content step]]]))
