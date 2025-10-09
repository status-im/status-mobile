(ns status-im.contexts.profile.backup-recovery-phrase.view
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.context :as quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.events-helper :as events-helper]
    [status-im.contexts.profile.backup-recovery-phrase.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(def ^:private step-labels
  [:t/backup-step-1 :t/backup-step-2 :t/backup-step-3 :t/backup-step-4])

(defn- header
  [{:keys [back-button? shell?]}]
  [:<>
   [quo/page-nav
    (cond-> {:icon-name           (if back-button? :i/arrow-left :i/close)
             :on-press            events-helper/navigate-back
             :accessibility-label :top-bar}
      shell? (assoc :background :blur))]
   [quo/page-top
    {:title            (i18n/label :t/backup-recovery-phrase)
     :description      :text
     :description-text (i18n/label :t/backup-recovery-phrase-description)}]])

(defn- word-item
  [{:keys [index text]}]
  [rn/view {:style style/word-item}
   [quo/counter {:type :grey :blur? true}
    index]
   [quo/text text]])

(defn words-column
  [{:keys [starting-index words]}]
  [rn/view {:style style/words-colum}
   (map-indexed (fn [idx word]
                  (let [word-number (+ starting-index idx)]
                    (with-meta [word-item {:index word-number :text word}]
                               {:key word-number})))
                words)])

(defn- seed-phrase-words
  [{:keys [seed-phrase theme shell?]}]
  (let [[first-column second-column] (split-at 6 seed-phrase)]
    [rn/view {:style (style/seed-phrase-container theme shell?)}
     [words-column
      {:starting-index 1
       :words          first-column}]
     [rn/view {:style (style/separator theme shell?)}]
     [words-column
      {:starting-index 7
       :words          second-column}]]))

(defn- seed-phrase-container
  [{:keys [shell? seed-phrase revealed?]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style style/seed-phrase}
     [seed-phrase-words
      {:seed-phrase seed-phrase
       :theme       theme
       :shell?      shell?}]
     (when-not revealed?
       [rn/view {:style [rn/stylesheet-absolute-fill style/blur-container]}
        [quo/blur
         {:style       (style/seed-phrase-blur-overlay shell? theme)
          :blur-type   :transparent
          :blur-amount 25}]])]))

(defn- step-item
  [{:keys [step-idx toggle-step-check shell?]}]
  (let [step-text (get step-labels step-idx)
        on-press  #(toggle-step-check step-idx)]
    (fn [{:keys [checked?]}]
      (let [customization-color (rf/sub [:profile/customization-color])]
        [rn/pressable
         {:style    style/step-item
          :hit-slop {:top 8 :bottom 8}
          :on-press on-press}
         [quo/selectors
          {:type                :checkbox
           :checked?            checked?
           :customization-color customization-color
           :on-change           on-press
           :blur?               (when shell? true)}]
         [quo/text (i18n/label step-text)]]))))

(defn- instructions
  [{:keys [step-checks toggle-step-check shell?]}]
  [rn/view {:style style/instructions}
   [quo/text
    {:style  style/instructions-header
     :weight :semi-bold}
    (i18n/label :t/how-to-backup)]
   (map-indexed (fn [idx step-label]
                  ^{:key (str step-label)}
                  [step-item
                   {:step-idx          idx
                    :toggle-step-check toggle-step-check
                    :checked?          (get step-checks idx)
                    :shell?            shell?}])
                step-labels)])

(defn bottom-button
  [{:keys [step-checks revealed? customization-color shell? on-success reveal-seed-phrase]}]
  (let [text      (i18n/label (if revealed? :t/i-have-written :t/reveal-phrase))
        disabled? (and (some false? step-checks) (not revealed?))
        on-press  (if revealed? on-success reveal-seed-phrase)]
    [quo/bottom-actions
     {:container-style  {:margin-top :auto}
      :actions          :one-action
      :blur?            shell?
      :button-one-label text
      :button-one-props {:disabled?           disabled?
                         :customization-color customization-color
                         :on-press            on-press}
      :description      (when revealed? :bottom)
      :description-text (when revealed? (i18n/label :t/next-you-will))}]))

(defn view
  []
  (let [{:keys             [on-success masked-seed-phrase shell? back-button?]
         initial-revealed? :revealed?} (quo.context/use-screen-params)
        [revealed? set-revealed]       (rn/use-state initial-revealed?)
        reveal-seed-phrase             (rn/use-callback #(set-revealed true))
        [seed-phrase set-seed-phrase]  (rn/use-state (or (some-> masked-seed-phrase
                                                                 (security/safe-unmask-data)
                                                                 (string/split #"\s"))
                                                         (vec (repeat 12 "-"))))
        on-complete                    (rn/use-callback
                                        #(on-success (security/mask-data seed-phrase))
                                        [seed-phrase])
        [step-checks set-step-checks]  (rn/use-state (-> step-labels count (repeat false) vec))
        toggle-step-check              (rn/use-callback
                                        (fn [idx]
                                          (set-step-checks #(update % idx not))))
        customization-color            (rf/sub [:profile/customization-color])]
    (rn/use-mount
     (fn []
       (when-not masked-seed-phrase
         (native-module/get-random-mnemonic
          (fn [new-phrase]
            (set-seed-phrase (string/split new-phrase #"\s")))))))
    [quo/overlay
     {:type    (when shell? :shell)
      :insets? true}
     [header
      {:back-button? back-button?
       :shell?       shell?}]
     [seed-phrase-container
      {:shell?      shell?
       :seed-phrase seed-phrase
       :revealed?   revealed?}]
     (when-not revealed?
       [instructions
        {:step-checks       step-checks
         :toggle-step-check toggle-step-check
         :shell?            shell?}])
     [bottom-button
      {:step-checks         step-checks
       :revealed?           revealed?
       :customization-color customization-color
       :shell?              shell?
       :on-success          on-complete
       :reveal-seed-phrase  reveal-seed-phrase}]]))
