(ns status-im.ui.screens.profile.seed.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.common.styles :as components.common.styles]
            [clojure.string :as string]
            [status-im.utils.config :as config]
            [status-im.utils.utils :as utils]
            [status-im.ui.screens.profile.seed.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as common.styles]))

(def steps-numbers
  {:intro       1
   :12-words    1
   :first-word  2
   :second-word 3
   :finish      3})

(defn step-back [step]
  (case step
    (:intro :12-words) (re-frame/dispatch [:navigate-back])
    :first-word (re-frame/dispatch [:my-profile/set-step :12-words])
    :second-word (re-frame/dispatch [:my-profile/set-step :first-word])))

(defn intro []
  [react/view {:style styles/intro-container}
   [components.common/image-contain {:container-style styles/intro-image}
    (:lock resources/ui)]
   [react/text {:style styles/intro-text}
    (i18n/label :t/your-data-belongs-to-you)]
   [react/text {:style styles/intro-description}
    (i18n/label :t/your-data-belongs-to-you-description)]
   [components.common/button {:button-style styles/intro-button
                              :on-press     #(re-frame/dispatch [:set-in [:my-profile/seed :step] :12-words])
                              :label        (i18n/label :t/ok-continue)}]])

(defn six-words [words]
  [react/view {:style styles/six-words-container}
   (for [[i word] words]
     ^{:key (str "word" i)}
     [react/view
      [react/view {:style styles/six-word-row}
       [react/text {:style styles/six-word-num}
        (inc i)]
       [react/text {:style styles/six-words-word}
        word]]
      (when (not= i (first (last words)))
        [react/view {:style styles/six-words-separator}])])])

(defview twelve-words [{:keys [mnemonic]}]
  (letsubs [mnemonic-vec (vec (map-indexed vector (clojure.string/split mnemonic #" ")))
            ref (reagent/atom nil)]
    {:component-did-mount (fn [_] (when config/testfairy-enabled?
                                    ;; NOTE(dmitryn) Doesn't work on Android without setTimeout
                                    (js/setTimeout
                                      #(.hideView js-dependencies/testfairy @ref)
                                      100)))}
    [react/view {:style styles/twelve-words-container}
     [react/text {:style styles/twelve-words-label}
      (i18n/label :t/your-seed-phrase)]
     [react/view {:style styles/twelve-words-columns
                  :ref (partial reset! ref)}
      [six-words (subvec mnemonic-vec 0 6)]
      [react/view {:style styles/twelve-words-columns-separator}]
      [six-words (subvec mnemonic-vec 6 12)]]
     [react/text {:style styles/twelve-words-description}
      (i18n/label :t/your-seed-phrase-description)]
     [react/view styles/twelve-words-spacer]
     [react/view styles/twelve-words-button-container
      [components.common/bottom-button
       {:forward? true
        :on-press #(re-frame/dispatch [:my-profile/enter-two-random-words])}]]]))

(defview input [error]
  [text-input/text-input-with-label
   {:placeholder    (i18n/label :t/enter-word)
    :auto-focus     true
    :on-change-text #(re-frame/dispatch [:set-in [:my-profile/seed :word] %])
    :error          error}])

(defn enter-word [step [idx word] error entered-word]
  ^{:key word}
  [react/view {:style styles/enter-word-container}
   [react/view {:style styles/enter-word-row}
    [react/text {:style styles/enter-word-label}
     (i18n/label :t/check-your-seed)]
    [react/text {:style styles/enter-word-n}
     (i18n/label :t/word-n {:number (inc idx)})]]
   [input error]
   [react/text {:style styles/enter-word-n-description}
    (i18n/label :t/word-n-description {:number (inc idx)})]
   [react/view styles/twelve-words-spacer]
   [react/view styles/twelve-words-button-container
    [components.common/bottom-button
     {:forward?  (not= :second-word step)
      :label     (when (= :second-word step) (i18n/label :t/done))
      :disabled? (string/blank? entered-word)
      :on-press  (fn [_]
                   (cond (not= word entered-word)
                         (re-frame/dispatch [:set-in [:my-profile/seed :error] (i18n/label :t/wrong-word)])

                         (= :first-word step)
                         (re-frame/dispatch [:my-profile/set-step :second-word])

                         :else
                         (utils/show-question
                           (i18n/label :t/are-you-sure?)
                           (i18n/label :t/are-you-sure-description)
                           #(re-frame/dispatch [:my-profile/finish]))))}]]])

(defn finish []
  [react/view {:style styles/finish-container}
   [react/view {:style styles/finish-logo-container}
    [react/view {:style (components.common.styles/logo-container 80 true)}
     [icons/icon :icons/ok styles/ok-icon]]]
   [react/text {:style styles/finish-label}
    (i18n/label :t/you-are-all-set)]
   [react/text {:style styles/finish-description}
    (i18n/label :t/you-are-all-set-description)]
   [components.common/button {:button-style styles/finish-button
                              :on-press     #(re-frame/dispatch [:navigate-back])
                              :label        (i18n/label :t/ok-got-it)}]])

(defview backup-seed []
  (letsubs [current-account [:get-current-account]
            {:keys [step first-word second-word error word]} [:get :my-profile/seed]]
    [react/keyboard-avoiding-view {:style common.styles/flex}
     [status-bar/status-bar]
     [toolbar/toolbar
      nil
      (when-not (#{:finish} step)
        (toolbar/nav-button (actions/back #(step-back step))))
      [react/view
       [react/text {:style styles/backup-seed}
        (i18n/label :t/backup-seed-phrase)]
       [react/text {:style styles/step-n}
        (i18n/label :t/step-i-of-n {:step (steps-numbers step) :number 3})]]]
     [components.common/separator]
     (case step
       :intro [intro]
       :12-words [twelve-words current-account]
       :first-word [enter-word step first-word error word]
       :second-word [enter-word step second-word error word]
       :finish [finish])]))