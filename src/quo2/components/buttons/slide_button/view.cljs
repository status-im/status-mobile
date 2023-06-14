(ns quo2.components.buttons.slide-button.view

  (:require [react-native.core :as rn :refer [use-effect]]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [oops.core :as oops]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as quo2.icons]
            [react-native.reanimated :as reanimated]
            [react-native.gesture :as gesture]
            [quo2.components.buttons.slide-button.style :as style]))

(defn drag-gesture
  [toggled translate-x total-swipeable-width initial-translate-x disabled on-end]
  (->
    (gesture/gesture-pan)
    (gesture/enabled (not disabled))

    (gesture/on-update
     (fn [event]
       (let [newvalue (if @toggled (+ total-swipeable-width event.translationX) event.translationX)]
         (when (and (<= 0 newvalue) (>= total-swipeable-width newvalue))
           (reanimated/set-shared-value
            translate-x
            (+ newvalue initial-translate-x))))))
    (gesture/on-end (fn []
                      (if (> (/ total-swipeable-width 2) (reanimated/get-shared-value translate-x))
                        (do (reanimated/set-shared-value
                             translate-x
                             initial-translate-x)
                            (reset! toggled false))
                        (do (reanimated/set-shared-value
                             translate-x
                             (+ total-swipeable-width initial-translate-x))
                            (on-end)
                            (reset! toggled true)))))))

(defn f-view
  "Options
   - `disabled` Boolean to disable the component.
   - `label`  label text.
   - `size` Size of the Slide Button.
   - `on-end` Callback called when the slide position reached the end."
  [{:keys [size disabled label on-end]
    :or   {size     "large"
           disabled false
           label    "Slide to sign"}} slider-width]

  (let [toggled               (reagent/atom false)
        knob-width            (if (= size "large") 40 32)
        slider-padding        4
        slider-height         (if (= size "large") 48 40)
        total-swipeable-width (- @slider-width knob-width (* slider-padding 2))
        initial-translate-x   (+ knob-width slider-padding)
        translate-x           (reanimated/use-shared-value initial-translate-x)
        on-layout             (fn [event]
                                (let [width (oops/oget event "nativeEvent.layout.width")]
                                  (reset! slider-width width)))]

    ;; (use-effect (fn []
    ;;               (reanimated/set-shared-value
    ;;                translate-x
    ;;                initial-translate-x))
    ;;             [size disabled])

    [rn/view {:flex 1}
     [reanimated/view
      {:on-layout on-layout
       :style     (style/slide-container slider-height slider-padding @slider-width disabled)}
      [reanimated/view
       {:style (style/foreground-pallet translate-x knob-width slider-padding)}
       [gesture/gesture-detector
        {:gesture
         (drag-gesture toggled translate-x total-swipeable-width initial-translate-x disabled on-end)
         :enabled? true}
        [rn/view {:style (style/knob knob-width slider-padding)}
         [quo2.icons/icon :i/arrow-right {:size 20 :color colors/white}]]
       ]]
      [rn/view {:style style/knob-icon}
       [quo2.icons/icon :i/face-id {:size 20 :color colors/primary-50}]
       [text/text
        {:accessibility-label :slider-button-label
         :size                :paragraph-1
         :weight              :medium
         :style               style/text}
        label]]]]))

(defn view
  [props]
  (let [slider-width (reagent/atom "100%")]
    [:f> f-view props slider-width]))