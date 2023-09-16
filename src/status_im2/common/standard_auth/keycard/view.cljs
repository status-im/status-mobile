(ns status-im2.common.standard-auth.keycard.view
  (:require [react-native.core :as rn]
            [quo2.core :as quo]
            [status-im2.common.standard-auth.keycard.style :as style]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [status-im2.common.resources :as resources]
            [utils.i18n :as i18n]
            [quo2.components.keycard.view :as keycard]
            [utils.re-frame :as rf]))

(defn handle-number-entry
  [entered-numbers max-digits max-attempt-reached number]
  (when (>= (count @entered-numbers) max-digits)
    (reset! entered-numbers []))
  (when (= (count @entered-numbers) (dec max-digits))
    (reset! max-attempt-reached true))
  (swap! entered-numbers conj number))

(defn enter-keycard-pin-sheet
  []
  (let [entered-numbers (reagent/atom [])
        max-digits 6
        max-attempt-reached (reagent/atom false)]

    (fn []
      (println "entered-numbers" @entered-numbers)

      [rn/view {:style style/container}
       [rn/view {:style style/inner-container}
        [quo/text {:accessibility-label :enter-keycard-pin-text :size :heading-2 :weight :semi-bold}
         (i18n/label :t/enter-keycard-pin)]

        [rn/view {:style style/context-tag}
         [quo/context-tag
          {:type    :icon
           :icon    :i/placeholder
           :size    24
           :context (i18n/label :t/card-with-name {:name "Alisher"})}]]

        [rn/view
         (if @max-attempt-reached
           {:style style/digits-container :margin-bottom 0}
           {:style style/digits-container :margin-bottom 34})
         (for [i (range max-digits)]
           [rn/view
            {:key   i
             :style (style/digit-style @max-attempt-reached i entered-numbers)}])]

        (when @max-attempt-reached
          [rn/view {:style style/max-attempt-reached-container}
           [quo/text {:size :label :style {:color colors/danger}}
            (i18n/label :t/attempts-left {:attempts 4})]])]

       [quo/numbered-keyboard
        {:disabled?   false
         :on-press    (fn [number] (handle-number-entry entered-numbers max-digits max-attempt-reached number))
         :blur?       false
         :delete-key? false
         :left-action :none}]])))

(defn this-is-not-keycard-sheet
  []
  [rn/view {:style style/container}
   [rn/view {:style style/inner-container}
    [quo/button
     {:container-style style/close-button
      :type            :grey
      :icon-only?      true
      :size            32
      :on-press        #(rf/dispatch [:hide-bottom-sheet])} :i/close]

    [quo/text
     {:accessibility-label :this-is-not-keycard-text :size :heading-1 :weight :semi-bold}
     (i18n/label :t/this-is-not-keycard)]

    [quo/text
     {:accessibility-label :make-sure-text
      :size                :paragraph-1
      :weight              :medium
      :style               style/secondary-text} (i18n/label :t/make-sure-scanned-keycard)]]

   [rn/image
    {:resize-mode :contain
     :style  {:aspect-ratio  1
              :height        503
              :margin-bottom 20
              :align-self    :center}
     :source      (resources/get-image :this-is-not-keycard)}]

   [rn/view {:style style/try-again-button}
    [quo/button
     {:type :primary}
     (i18n/label :t/try-again)]]])

(defn keycard-locked-sheet
  []
  [rn/view {:style style/container :height (:height (rn/get-window))}
   [rn/view {:style style/inner-container}
    [quo/button
     {:container-style style/close-button
      :type            :grey
      :icon-only?      true
      :size            32
      :on-press        #(rf/dispatch [:hide-bottom-sheet])} :i/close]

    [quo/text
     {:accessibility-label :keycard-locked-text :size :heading-1 :weight :semi-bold}
     (i18n/label :t/keycard-locked)]

    [quo/text
     {:accessibility-label :unlock-keycard-text
      :size                :paragraph-1
      :weight              :medium
      :style               style/secondary-text} (i18n/label :t/unlock-keycard-to-use)]

    [rn/view {:style style/keycard}
     [keycard/keycard
      {:holder-name "Alisher"
       :locked?     true}]]

    [quo/button
     {:type :primary}
     (i18n/label :t/unlock-keycard)]

    [quo/divider-label {:tight? false :container-style style/divider} (i18n/label :t/other-options)]

    [quo/button
     {:type            :ghost
      :size            20
      :icon-left       :i/refresh
      :container-style style/reset-keycard-button} (i18n/label :t/factory-reset-keycard)]]])

(defn wrong-keycard-sheet
  []
  [rn/view {:style style/container}
   [rn/view {:style style/inner-container}
    [quo/button
     {:container-style style/close-button
      :type            :grey
      :icon-only?      true
      :size            32
      :on-press        #(rf/dispatch [:hide-bottom-sheet])} :i/close]

    [quo/text
     {:accessibility-label :wrong-card-text :size :heading-1 :weight :semi-bold}
     (i18n/label :t/wrong-keycard)]

    [quo/text
     {:accessibility-label :make-sure-scanned-text
      :size                :paragraph-1
      :weight              :medium
      :style               {:margin-top 8 :margin-bottom 20}}
     (i18n/label :t/make-sure-scanned-card-contains-keys)]]

   [rn/image
    {:resize-mode :contain
     :style  {:aspect-ratio  1
              :height        481
              :margin-bottom 20
              :align-self    :center}
     :source (resources/get-image :wrong-keycard)}]

   [rn/view {:style style/try-again-button}
    [quo/button
     {:type :primary}
     (i18n/label :t/try-again)]]])
