(ns status-im.contexts.wallet.create-account.new-keypair.check-your-backup.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.temp :as temp]
    [status-im.contexts.wallet.create-account.new-keypair.check-your-backup.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn random-selection
  []
  (->> (range 12)
       (shuffle)
       (take 4)
       vec))

(defn random-words-with-string
  [array given-string]
  (let [random-words (->> array
                          (remove #(= % given-string))
                          (shuffle)
                          (take 3))
        result       (conj random-words given-string)]
    (->> result
         (shuffle)
         vec)))

(defn cheat-warning
  []
  [rn/view
   [quo/drawer-top {:title (i18n/label :t/do-not-cheat)}]
   [quo/text
    {:style {:padding-horizontal 20
             :padding-top        4
             :padding-bottom     8}} (i18n/label :t/do-not-cheat-description)]
   [quo/bottom-actions
    {:button-one-label (i18n/label :t/see-recovery-phrase-again)
     :button-one-props {:customization-color :blue
                        :on-press            (fn []
                                               (rf/dispatch [:hide-bottom-sheet])
                                               (rf/dispatch [:navigate-back {:revealed? true}]))}}]])

(defn button
  [{:keys [word current-word quiz-index incorrect-count show-error? margin-right]}]
  [quo/button
   {:type            :grey
    :on-press        (fn []
                       (if (= word current-word)
                         (do
                           (reset! quiz-index (inc @quiz-index))
                           (reset! incorrect-count 0)
                           (reset! show-error? false))
                         (do
                           (when (> @incorrect-count 0)
                             (rf/dispatch [:show-bottom-sheet
                                           {:content cheat-warning}]))
                           (reset! incorrect-count (inc @incorrect-count))
                           (reset! show-error? true))))
    :container-style (style/button margin-right)} word])

(defn buttons-row
  [{:keys [margin-bottom options] :as params}]
  [rn/view {:style (style/buttons-inner-container margin-bottom)}
   [button
    (merge params
           {:word         (first options)
            :margin-right 12})]
   [button (merge params {:word (second options)})]])

(defn- view-internal
  []
  (let [random-indices  (random-selection)
        quiz-index      (reagent/atom 0)
        incorrect-count (reagent/atom 0)
        show-error?     (reagent/atom false)]
    (fn []
      (let [current-word-index (get random-indices (min @quiz-index 3))
            current-word       (get temp/secret-phrase current-word-index)
            options            (random-words-with-string temp/random-words current-word)
            button-params      {:quiz-index      quiz-index
                                :current-word    current-word
                                :incorrect-count incorrect-count
                                :show-error?     show-error?}]
        [rn/view {:style {:flex 1}}
         [quo/page-nav
          {:icon-name           :i/arrow-left
           :on-press            #(rf/dispatch [:navigate-back])
           :accessibility-label :top-bar}]
         [quo/text-combinations
          {:container-style style/header-container
           :title           (i18n/label :t/check-your-backup)
           :description     (i18n/label :t/confirm-the-position)}]
         [rn/flat-list
          {:data                    random-indices
           :render-fn               (fn [num index] [quo/quiz-item
                                                     {:state    (if (= @quiz-index index)
                                                                  (if (and (pos? @incorrect-count)
                                                                           @show-error?)
                                                                    :error
                                                                    :empty)
                                                                  (if (> @quiz-index index)
                                                                    :success
                                                                    :disabled))
                                                      :word     (get temp/secret-phrase num)
                                                      :number   (inc num)
                                                      :on-press #(when (= @quiz-index index)
                                                                   (reset! show-error? false))}])
           :separator               [rn/view {:style {:height 8}}]
           :content-container-style {:padding-horizontal 20}}]
         [rn/view {:style style/buttons-container}
          [buttons-row
           (merge button-params
                  {:margin-bottom 12
                   :options       (subvec options 0 2)})]
          [buttons-row (merge button-params {:options (subvec options 2 4)})]]]))))

(def view (quo.theme/with-theme view-internal))
