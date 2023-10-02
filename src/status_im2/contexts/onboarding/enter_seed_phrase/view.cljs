(ns status-im2.contexts.onboarding.enter-seed-phrase.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im2.constants :as constants]
            [status-im2.contexts.onboarding.enter-seed-phrase.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(def ^:private max-seed-phrase-length
  (apply max constants/seed-phrase-valid-length))

(defn- partial-word-in-dictionary?
  [partial-word]
  (some #(string/starts-with? % partial-word) mnemonic/dictionary))

(defn- word-in-dictionary?
  [word]
  (some #(= % word) mnemonic/dictionary))

(def ^:private partial-word-not-in-dictionary?
  (comp not partial-word-in-dictionary?))

(def ^:private word-not-in-dictionary?
  (comp not word-in-dictionary?))

(defn- header
  [seed-phrase-count]
  [rn/view {:style style/header-container}
   [quo/text {:weight :semi-bold :size :heading-1}
    (i18n/label :t/use-recovery-phrase)]
   [rn/view {:style style/word-count-container}
    [quo/text
     {:style  {:color colors/white-opa-40}
      :weight :regular
      :size   :paragraph-2}
     (i18n/label-pluralize seed-phrase-count :t/words-n)]]])

(defn- clean-seed-phrase
  [seed-phrase]
  (-> seed-phrase
      (string/lower-case)
      (string/replace #", " " ")
      (string/replace #"," " ")
      (string/replace #"\s+" " ")
      (string/trim)))

(defn- recovery-form
  [{:keys [seed-phrase word-count error-state? all-words-valid? on-change-seed-phrase
           keyboard-shown? on-submit]}]
  (let [button-disabled? (or error-state?
                             (not (constants/seed-phrase-valid-length word-count))
                             (not all-words-valid?))]
    [rn/view {:style style/form-container}
     [header word-count]
     [rn/view {:style style/input-container}
      [quo/recovery-phrase-input
       {:accessibility-label      :passphrase-input
        :placeholder              (i18n/label :t/seed-phrase-placeholder)
        :auto-capitalize          :none
        :auto-correct             false
        :auto-focus               true
        :mark-errors?             true
        :word-limit               max-seed-phrase-length
        :error-pred-current-word  partial-word-not-in-dictionary?
        :error-pred-written-words word-not-in-dictionary?
        :on-change-text           on-change-seed-phrase}
       seed-phrase]]
     [quo/button
      {:container-style (style/continue-button keyboard-shown?)
       :type            :primary
       :disabled?       button-disabled?
       :on-press        on-submit}
      (i18n/label :t/continue)]]))

(defn keyboard-suggestions
  [current-word]
  (->> mnemonic/dictionary
       (filter #(string/starts-with? % current-word))
       (take 7)))

(defn screen
  []
  (reagent/with-let [keyboard-shown?         (reagent/atom false)
                     keyboard-show-listener  (.addListener rn/keyboard
                                                           "keyboardDidShow"
                                                           #(reset! keyboard-shown? true))
                     keyboard-hide-listener  (.addListener rn/keyboard
                                                           "keyboardDidHide"
                                                           #(reset! keyboard-shown? false))
                     invalid-seed-phrase?    (reagent/atom false)
                     set-invalid-seed-phrase #(reset! invalid-seed-phrase? true)
                     seed-phrase             (reagent/atom "")
                     on-change-seed-phrase   (fn [new-phrase]
                                               (when @invalid-seed-phrase?
                                                 (reset! invalid-seed-phrase? false))
                                               (reset! seed-phrase new-phrase))
                     on-submit               (fn []
                                               (swap! seed-phrase clean-seed-phrase)
                                               (rf/dispatch [:onboarding-2/seed-phrase-entered
                                                             (security/mask-data @seed-phrase)
                                                             set-invalid-seed-phrase]))]
    (let [words-coll               (mnemonic/passphrase->words @seed-phrase)
          last-word                (peek words-coll)
          pick-suggested-word      (fn [pressed-word]
                                     (swap! seed-phrase str (subs pressed-word (count last-word)) " "))
          ;; Last word doesn't exist in dictionary while being written by the user, so
          ;; it's validated checking whether is a substring of a dictionary word or not.
          last-partial-word-valid? (partial-word-in-dictionary? last-word)
          last-word-valid?         (word-in-dictionary? last-word)
          butlast-words-valid?     (every? word-in-dictionary? (butlast words-coll))
          all-words-valid?         (and butlast-words-valid? last-word-valid?)
          word-count               (mnemonic/words-count @seed-phrase)
          words-exceeded?          (> word-count max-seed-phrase-length)
          error-in-words?          (or (not last-partial-word-valid?)
                                       (not butlast-words-valid?))
          upper-case?              (boolean (re-find #"[A-Z]" @seed-phrase))
          suggestions-state        (cond
                                     (or error-in-words?
                                         words-exceeded?
                                         @invalid-seed-phrase?)           :error
                                     (string/blank? @seed-phrase)         :info
                                     (string/ends-with? @seed-phrase " ") :empty
                                     :else                                :words)
          suggestions-text         (cond
                                     upper-case?           (i18n/label :t/seed-phrase-words-uppercase)
                                     words-exceeded?       (i18n/label :t/seed-phrase-words-exceeded)
                                     error-in-words?       (i18n/label :t/seed-phrase-error)
                                     @invalid-seed-phrase? (i18n/label :t/seed-phrase-invalid)
                                     :else                 (i18n/label :t/seed-phrase-info))]
      [:<>
       [recovery-form
        {:seed-phrase           @seed-phrase
         :error-state?          (= suggestions-state :error)
         :all-words-valid?      all-words-valid?
         :on-change-seed-phrase on-change-seed-phrase
         :word-count            word-count
         :on-submit             on-submit
         :keyboard-shown?       @keyboard-shown?}]
       (when @keyboard-shown?
         [rn/view {:style style/keyboard-container}
          [quo/predictive-keyboard
           {:type     suggestions-state
            :blur?    true
            :text     suggestions-text
            :words    (keyboard-suggestions last-word)
            :on-press pick-suggested-word}]])])
    (finally
     (.remove keyboard-show-listener)
     (.remove keyboard-hide-listener))))

(defn enter-seed-phrase
  []
  (let [{navigation-bar-top :top} (safe-area/get-insets)]
    [rn/view {:style style/full-layout}
     [rn/keyboard-avoiding-view {:style style/page-container}
      [quo/page-nav
       {:margin-top navigation-bar-top
        :background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back-within-stack :new-to-status])}]
      [screen]]]))
