(ns status-im.common.enter-seed-phrase.view
  (:require
    [clojure.string :as string]
    [legacy.status-im.ethereum.mnemonic :as mnemonic]
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.enter-seed-phrase.style :as style]
    [status-im.constants :as constants]
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
  [text seed-phrase-count]
  [rn/view {:style style/header-container}
   [quo/text {:weight :semi-bold :size :heading-1}
    text]
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

(defn- secure-clean-seed-phrase
  [seed-phrase]
  (-> seed-phrase
      security/safe-unmask-data
      clean-seed-phrase
      security/mask-data))

(defn- recovery-phrase-form
  [{:keys [keypair title seed-phrase word-count on-change-seed-phrase ref]} & children]
  (->> children
       (into
        [rn/view {:style style/form-container}
         [header title word-count]
         (when keypair
           [quo/context-tag
            {:type            :icon
             :container-style {:padding-top 8}
             :icon            :i/seed-phrase
             :size            24
             :blur?           true
             :context         (:name keypair)}])
         [rn/view {:style style/input-container}
          [quo/recovery-phrase-input
           {:accessibility-label      :passphrase-input
            :ref                      ref
            :placeholder              (i18n/label :t/seed-phrase-placeholder)
            :placeholder-text-color   colors/white-opa-30
            :auto-capitalize          :none
            :auto-correct             false
            :auto-focus               true
            :mark-errors?             true
            :word-limit               max-seed-phrase-length
            :error-pred-current-word  partial-word-not-in-dictionary?
            :error-pred-written-words word-not-in-dictionary?
            :on-change-text           on-change-seed-phrase}
           seed-phrase]]])))

(defn keyboard-suggestions
  [current-word]
  (->> mnemonic/dictionary
       (filter #(string/starts-with? % current-word))
       (take 7)))

(defn recovery-phrase-screen
  [{:keys [banner-offset initial-insets keypair title recovering-keypair? render-controls]}]
  (reagent/with-let [keyboard-shown?           (reagent/atom false)
                     keyboard-show-listener    (.addListener rn/keyboard
                                                             "keyboardDidShow"
                                                             #(reset! keyboard-shown? true))
                     keyboard-hide-listener    (.addListener rn/keyboard
                                                             "keyboardDidHide"
                                                             #(reset! keyboard-shown? false))
                     invalid-seed-phrase?      (reagent/atom false)
                     incorrect-seed-phrase?    (reagent/atom false)
                     input-ref                 (reagent/atom nil)
                     focus-input               #(some-> @input-ref
                                                        (oops/ocall "focus"))
                     set-incorrect-seed-phrase #(reset! incorrect-seed-phrase? true)
                     set-invalid-seed-phrase   #(reset! invalid-seed-phrase? true)
                     seed-phrase               (reagent/atom "")
                     on-change-seed-phrase     (fn [new-phrase]
                                                 (when @invalid-seed-phrase?
                                                   (reset! invalid-seed-phrase? false))
                                                 (when @incorrect-seed-phrase?
                                                   (reset! incorrect-seed-phrase? false))
                                                 (reset! seed-phrase new-phrase))
                     on-submit                 (fn []
                                                 (swap! seed-phrase clean-seed-phrase)
                                                 (if recovering-keypair?
                                                   (rf/dispatch [:wallet/seed-phrase-entered
                                                                 (security/mask-data
                                                                  @seed-phrase)
                                                                 set-invalid-seed-phrase])
                                                   (rf/dispatch [:onboarding/seed-phrase-entered
                                                                 (security/mask-data @seed-phrase)
                                                                 set-invalid-seed-phrase])))]
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
                                         @invalid-seed-phrase?
                                         @incorrect-seed-phrase?)         :error
                                     (string/blank? @seed-phrase)         :info
                                     (string/ends-with? @seed-phrase " ") :empty
                                     :else                                :words)
          suggestions-text         (cond
                                     upper-case?             (i18n/label :t/seed-phrase-words-uppercase)
                                     words-exceeded?         (i18n/label :t/seed-phrase-words-exceeded)
                                     error-in-words?         (i18n/label :t/seed-phrase-error)
                                     @invalid-seed-phrase?   (i18n/label :t/seed-phrase-invalid)
                                     @incorrect-seed-phrase? (i18n/label :t/seed-phrase-incorrect)
                                     :else                   (i18n/label :t/seed-phrase-info))
          error-state?             (= suggestions-state :error)
          button-disabled?         (or error-state?
                                       (not (constants/seed-phrase-valid-length word-count))
                                       (not all-words-valid?))]
      [rn/view
       {:style (style/recovery-phrase-container {:insets          initial-insets
                                                 :banner-offset   banner-offset
                                                 :keyboard-shown? @keyboard-shown?})}
       [recovery-phrase-form
        {:title                 title
         :keypair               keypair
         :seed-phrase           @seed-phrase
         :on-change-seed-phrase on-change-seed-phrase
         :word-count            word-count
         :ref                   #(reset! input-ref %)}
        (if (fn? render-controls)
          (render-controls {:submit-disabled?          button-disabled?
                            :keyboard-shown?           @keyboard-shown?
                            :container-style           style/continue-button
                            :prepare-seed-phrase       secure-clean-seed-phrase
                            :focus-input               focus-input
                            :seed-phrase               (security/mask-data @seed-phrase)
                            :set-incorrect-seed-phrase set-incorrect-seed-phrase})
          [quo/button
           {:container-style style/continue-button
            :type            :primary
            :disabled?       button-disabled?
            :on-press        on-submit}
           (i18n/label :t/continue)])]
       (when (or @keyboard-shown? error-state?)
         [rn/view {:style style/keyboard-container}
          [quo/predictive-keyboard
           {:type     suggestions-state
            :blur?    (not recovering-keypair?)
            :text     suggestions-text
            :words    (keyboard-suggestions last-word)
            :on-press pick-suggested-word}]])])
    (finally
     (.remove keyboard-show-listener)
     (.remove keyboard-hide-listener))))

(defn screen
  [{:keys [title keypair navigation-icon recovering-keypair? render-controls]}]
  (let [[insets _]    (rn/use-state (safe-area/get-insets))
        banner-offset (rf/sub [:alert-banners/top-margin])]
    [rn/view {:style style/full-layout}
     [rn/keyboard-avoiding-view {:style style/page-container}
      [quo/page-nav
       {:margin-top (:top insets)
        :background :blur
        :icon-name  (or navigation-icon
                        (if recovering-keypair? :i/close :i/arrow-left))
        :on-press   #(rf/dispatch [:navigate-back])}]
      [recovery-phrase-screen
       {:title               title
        :keypair             keypair
        :render-controls     render-controls
        :banner-offset       banner-offset
        :initial-insets      insets
        :recovering-keypair? recovering-keypair?}]]]))

(defn view
  []
  (let [{:keys [recovering-keypair?]} (rf/sub [:get-screen-params])]
    (rn/use-unmount
     #(rf/dispatch [:onboarding/clear-navigated-to-enter-seed-phrase-from-screen]))
    [screen
     {:title               (i18n/label :t/use-recovery-phrase)
      :recovering-keypair? recovering-keypair?}]))
