(ns status-im2.common.standard-auth.view
  (:require [react-native.core :as rn]
            [quo2.components.numbered-keyboard.numbered-keyboard.view :as numbered-keyboard]
            [quo2.core :as quo]
            [status-im2.common.standard-auth.style :as style]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [react-native.fast-image :as fast-image]
            [status-im2.common.resources :as resources]
            [utils.i18n :as i18n]
            [quo2.components.keycard.view :as keycard]
            [utils.re-frame :as rf]))

(defn enter-keycard-pin-sheet
  []
  (let [entered-numbers (reagent/atom [])
        max-digits 6
        max-attempt-reached (reagent/atom false)]
    (fn []
      (defn handle-number-entry [number]
        (when (>= (count @entered-numbers) max-digits)
          (reset! entered-numbers []))
        (when (= (count @entered-numbers) (dec max-digits))
          (reset! max-attempt-reached true))
        (swap! entered-numbers conj number))

      (println "entered-numbers" @entered-numbers)

      (defn get-dot-style [idx]
        (if (<= idx (dec (count @entered-numbers)))
          (if @max-attempt-reached
            (assoc style/digit :backgroundColor colors/danger)
            (assoc style/digit :backgroundColor colors/white))
          (assoc style/digit :backgroundColor colors/neutral-50)))

      [rn/view {:style style/container}
       [rn/view {:style style/inner-container}
        [quo/text {:accessibility-label :enter-keycard-pin-text :size :heading-2 :weight :semi-bold} "Enter Keycard PIN"]

        [rn/view {:style style/context-tag}
         [quo/context-tag
          {:type           :icon
           :icon           :i/placeholder
           :size            24
           :context     "Alisher Card"}]]

        [rn/view
         (if @max-attempt-reached
           {:style style/digits-container :margin-bottom 0}
           {:style style/digits-container :margin-bottom 34})
         (for [i (range max-digits)]
           [rn/view
            {:key i
             :style (get-dot-style i)}])]

        (when @max-attempt-reached
          [rn/view {:style style/max-attempt-reached-container}
           [quo/text {:size :label :style {:color colors/danger}} "4 attempts left"]])]

       [numbered-keyboard/view
        {:disabled?   false
         :on-press    handle-number-entry
         :blur?       false
         :delete-key? false
         :left-action :none}]])))

(defn this-is-not-keycard-sheet
  []
  [rn/view {:style style/container}
   [rn/view {:style style/inner-container}
    [quo/button
     {:container-style style/close-button
      :type :grey
      :icon-only? true
      :size 32
      :on-press        #(rf/dispatch [:hide-bottom-sheet])} :i/close]

    [quo/text
     {:accessibility-label :this-is-not-keycard-text :size :heading-1 :weight :semi-bold} "Oops, this isn’t a Keycard"]

    [quo/text {:accessibility-label :make-sure-text :size :paragraph-1 :weight :medium :style style/secondary-text} "Make sure the card you scanned is a Keycard."]]

   [fast-image/fast-image
    {:style  {:width  "100%"
              :height 503
              :margin-bottom 20
              :align-self :center}
     :source (resources/get-image :this-is-not-keycard)}]

   [rn/view {:style style/try-again-button}
    [quo/button
     {:type            :primary}
     "Try again"]]])

(defn keycard-locked-sheet
  []
  [rn/view {:style style/container :height (:height (rn/get-window))}
   [rn/view {:style style/inner-container}
    [quo/button
     {:container-style style/close-button
      :type :grey
      :icon-only? true
      :size 32
      :on-press        #(rf/dispatch [:hide-bottom-sheet])} :i/close]

    [quo/text
     {:accessibility-label :keycard-locked-text :size :heading-1 :weight :semi-bold} "Oh no! Your Keycard is locked!"]

    [quo/text {:accessibility-label :unlock-keycard-text :size :paragraph-1 :weight :medium :style style/secondary-text} "Unlock Keycard to use keys stored on it"]

    [rn/view {:style style/keycard}
     [keycard/keycard
      {:holder-name "Alisher Card"
       :locked? true}]]

    [quo/button
     {:type            :primary}
     "Unlock Keycard"]

    [quo/divider-label {:tight? false :container-style style/divider} (i18n/label :t/other-options)]

    [quo/button
     {:type      :ghost
      :size      20
      :icon-left :i/refresh
      :container-style style/reset-keycard-button} "Factory reset this Keycard"]]])

(defn wrong-keycard-sheet
  []
  [rn/view {:style style/container}
   [rn/view {:style style/inner-container}
    [quo/button
     {:container-style style/close-button
      :type :grey
      :icon-only? true
      :size 32
      :on-press        #(rf/dispatch [:hide-bottom-sheet])} :i/close]

    [quo/text
     {:accessibility-label :wrong-card-text :size :heading-1 :weight :semi-bold} "Oops, wrong Keycard"]

    [quo/text {:accessibility-label :make-sure-scanned-text :size :paragraph-1 :weight :medium :style {:margin-top 8 :margin-bottom 20}} "Make sure the card you scanned is the one that contains your profile keys."]]

   [fast-image/fast-image
    {:style  {:width  "100%"
              :height 481
              :margin-bottom 20
              :align-self :center}
     :source (resources/get-image :wrong-keycard)}]

   [rn/view {:style style/try-again-button}
    [quo/button
     {:type            :primary}
     "Try again"]]])  
