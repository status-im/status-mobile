(ns status-im.contexts.wallet.add-account.create-account.new-keypair.confirm-backup.view
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.add-account.create-account.new-keypair.confirm-backup.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(def secret-words-count 12)

(def questions-count 4)

(defn- random-selection
  []
  (->> (range secret-words-count)
       (shuffle)
       (take questions-count)
       vec))

(defn- random-words-with-string
  [words given-string]
  (let [random-words (->> words
                          (remove #(= % given-string))
                          (shuffle)
                          (take (dec questions-count)))
        result       (shuffle (conj random-words given-string))]
    [(subvec result 0 2) (subvec result 2 4)]))

(defn- cheat-warning
  []
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/do-not-cheat)}]
     [quo/text
      {:style style/cheat-description}
      (i18n/label :t/do-not-cheat-description)]
     [quo/bottom-actions
      {:button-one-label (i18n/label :t/see-recovery-phrase-again)
       :button-one-props {:customization-color customization-color
                          :on-press            (fn []
                                                 (rf/dispatch [:hide-bottom-sheet])
                                                 (rf/dispatch [:navigate-back]))}}]]))

(defn- button
  [{:keys [word margin-right on-press]}]
  [quo/button
   {:type            :grey
    :on-press        #(on-press word)
    :container-style (style/button margin-right)} word])

(defn- buttons-row
  [{:keys [margin-bottom options] :as params}]
  [rn/view {:style (style/buttons-inner-container margin-bottom)}
   [button
    (assoc params
           :word         (first options)
           :margin-right 12)]
   [button (assoc params :word (second options))]])

(defn- complete-backup-sheet
  [on-success]
  (let [customization-color    (rf/sub [:profile/customization-color])
        [checked? set-checked] (rn/use-state false)]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/complete-backup)}]
     [quo/text
      {:style style/cheat-description}
      (i18n/label :t/ensure-written-recovery)]
     [quo/disclaimer
      {:checked?        checked?
       :container-style {:margin-horizontal 20}
       :on-change       #(set-checked (not checked?))}
      (i18n/label :t/written-seed-ready)]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/done)
       :button-one-props {:disabled?           (not checked?)
                          :customization-color customization-color
                          :on-press            (fn []
                                                 (rf/dispatch [:hide-bottom-sheet])
                                                 (on-success))}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press (fn []
                                      (rf/dispatch [:hide-bottom-sheet]))}}]]))

(defn view
  []
  (let [random-indices               (random-selection)
        quiz-index                   (reagent/atom 0)
        incorrect-count              (reagent/atom 0)
        show-error?                  (reagent/atom false)
        {:keys [on-success
                masked-seed-phrase]} (rf/sub [:get-screen-params])
        unmasked-seed-phrase         (security/safe-unmask-data masked-seed-phrase)
        random-phrase                (reagent/atom [])]
    (fn []
      (rn/use-mount
       (fn []
         (native-module/get-random-mnemonic #(reset! random-phrase (string/split % #"\s")))))
      (when-not (empty? @random-phrase)
        (let [current-word-index            (get random-indices
                                                 (min @quiz-index (dec questions-count)))
              current-word                  (get unmasked-seed-phrase current-word-index)
              [options-row-0 options-row-1] (random-words-with-string @random-phrase current-word)
              on-button-press               (fn [word]
                                              (if (= word current-word)
                                                (do
                                                  (when (< @quiz-index questions-count)
                                                    (reset! quiz-index (inc @quiz-index)))
                                                  (reset! incorrect-count 0)
                                                  (reset! show-error? false)
                                                  (when (and on-success (= @quiz-index questions-count))
                                                    (rf/dispatch [:show-bottom-sheet
                                                                  {:content (fn [] [complete-backup-sheet
                                                                                    on-success])}])))
                                                (do
                                                  (when (> @incorrect-count 0)
                                                    (rf/dispatch [:show-bottom-sheet
                                                                  {:content cheat-warning}]))
                                                  (reset! incorrect-count (inc @incorrect-count))
                                                  (reset! show-error? true))))]
          [rn/view {:style {:flex 1}}
           [quo/page-nav
            {:icon-name           :i/close
             :on-press            #(rf/dispatch [:navigate-back])
             :accessibility-label :top-bar}]
           [quo/page-top
            {:title            (i18n/label :t/confirm-backup)
             :description      :text
             :description-text (i18n/label :t/confirm-the-position)}]
           [rn/flat-list
            {:data                    random-indices
             :render-fn               (fn [num index]
                                        [quo/quiz-item
                                         {:state    (cond
                                                      (and (= @quiz-index index)
                                                           (pos? @incorrect-count)
                                                           @show-error?)
                                                      :error

                                                      (= @quiz-index index)
                                                      :empty

                                                      (> @quiz-index index)
                                                      :success

                                                      :else
                                                      :disabled)
                                          :word     (get unmasked-seed-phrase num)
                                          :number   (inc num)
                                          :on-press #(when (= @quiz-index index)
                                                       (reset! show-error? false))}])
             :separator               [rn/view {:style {:height 8}}]
             :content-container-style {:padding-horizontal 20}}]
           [rn/view {:style style/buttons-container}
            [buttons-row
             {:on-press      on-button-press
              :margin-bottom 12
              :options       options-row-0}]
            [buttons-row
             {:on-press on-button-press
              :options  options-row-1}]]])))))
