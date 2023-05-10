(ns status-im2.contexts.onboarding.enter-seed-phrase.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im2.constants :as constants]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [status-im2.contexts.onboarding.enter-seed-phrase.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(def button-disabled?
  (comp not constants/seed-phrase-valid-length mnemonic/words-count))

(defn clean-seed-phrase
  [s]
  (as-> s $
    (string/lower-case $)
    (string/split $ #"\s")
    (filter #(not (string/blank? %)) $)
    (string/join " " $)))

(defn page
  [{:keys [navigation-bar-top]}]
  (let [seed-phrase            (reagent/atom "")
        error-message          (reagent/atom "")
        on-invalid-seed-phrase #(reset! error-message (i18n/label :t/custom-seed-phrase))]
    (fn []
      [rn/view {:style style/page-container}
       [navigation-bar/navigation-bar {:top navigation-bar-top}]
       [rn/view {:style {:padding-horizontal 20}}
        [quo/text
         {:weight :bold
          :align  :center}
         (i18n/label :t/use-recovery-phrase)]
        [quo/text
         (i18n/label-pluralize (mnemonic/words-count @seed-phrase) :t/words-n)]
        [rn/view {:style style/input-container}
         [quo/recovery-phrase-input
          {:on-change-text      (fn [t]
                                  (reset! seed-phrase (clean-seed-phrase t))
                                  (reset! error-message ""))
           :mark-errors?        true
           :error-pred          (constantly false)
           :word-limit          24
           :auto-focus          true
           :accessibility-label :passphrase-input
           :placeholder         (i18n/label :t/seed-phrase-placeholder)
           :auto-correct        false}
          @seed-phrase]]
        [quo/button
         {:disabled (button-disabled? @seed-phrase)
          :on-press #(rf/dispatch [:onboarding-2/seed-phrase-entered
                                   (security/mask-data @seed-phrase)
                                   on-invalid-seed-phrase])}
         (i18n/label :t/continue)]
        (when (seq @error-message)
          [quo/text @error-message])]])))

(defn enter-seed-phrase
  []
  (let [{:keys [top]} (safe-area/get-insets)]
    [rn/view {:style {:flex 1}}
     [background/view true]
     [page {:navigation-bar-top top}]]))
