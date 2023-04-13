(ns status-im2.contexts.onboarding.enter-seed-phrase.view
  (:require [quo2.core :as quo]
            [quo.core :as quo1]
            [clojure.string :as string]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im2.constants :as constants]
            [utils.security.core :as security]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.enter-seed-phrase.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.i18n :as i18n]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?   true
     :left-section {:type                :blur-bg
                    :icon                :i/arrow-left
                    :icon-override-theme :dark
                    :on-press            #(rf/dispatch [:navigate-back])}
     :mid-section  {:type :text-only :main-text ""}}]])

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
  []
  (let [seed-phrase            (reagent/atom "")
        error-message          (reagent/atom "")
        on-invalid-seed-phrase #(reset! error-message (i18n/label :t/custom-seed-phrase))]
    (fn []
      [rn/view {:style style/page-container}
       [navigation-bar]
       [rn/view {:style {:padding-horizontal 20}}
        [quo/text
         {:weight :bold
          :align  :center}
         (i18n/label :t/use-recovery-phrase)]
        [quo/text
         (i18n/label-pluralize (mnemonic/words-count @seed-phrase) :t/words-n)]
        [:<>
         [quo1/text-input
          {:on-change-text      (fn [t]
                                  (reset! seed-phrase (clean-seed-phrase t))
                                  (reset! error-message ""))
           :auto-focus          true
           :accessibility-label :passphrase-input
           :placeholder         (i18n/label :t/seed-phrase-placeholder)
           :show-cancel         false
           :bottom-value        40
           :multiline           true
           :auto-correct        false
           :keyboard-type       :visible-password
           :monospace           true}]]
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
  [rn/view {:style {:flex 1}}
   [background/view true]
   [page]])
