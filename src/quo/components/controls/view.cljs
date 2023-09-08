(ns quo.components.controls.view
  (:require [cljs-bean.core :as bean]
            [quo.animated :as animated]
            [quo.components.controls.styles :as styles]
            [quo.design-system.colors :as colors]
            [quo.gesture-handler :as gh]
            [quo.react :as react]
            [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.icons :as icons]))

(defn control-builder
  [component]
  (fn [props]
    (let [{:keys [
                  ;                   value
                  onChange disabled]}
          (bean/bean props)
          state 0
          tap-state (:undetermined gh/states)
          tap-handler (animated/on-gesture {:state tap-state})
          hold (react/use-memo
                (fn []
                    ;commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
                    ;TODO: replace this with an updated implementation
                    ;                  (animated/with-timing-transition
                    ;                   (animated/eq tap-state (:began gh/states))
                    ;                   {})
                )
                [])
          transition (react/use-memo
                      (fn []
                        (animated/with-spring-transition state
                                                         (:lazy
                                                          animated/springs)))
                      [])
          ;commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
          ;TODO: replace this with an updated implementation
          ;          press-end (fn []
          ;                      (when (and (not disabled) onChange)
          ;                        (onChange (not value))))
         ]
      ;commented out to upgrade react-native-reanimated to v3 and react-native to 0.72
      ;TODO: replace this with an updated implementation
      ;      (animated/code!
      ;       (fn []
      ;         (animated/cond* (animated/eq tap-state (:end gh/states))
      ;                         [(animated/set state (animated/not* state))
      ;                          (animated/set tap-state (:undetermined gh/states))
      ;                          (animated/call* [] press-end)]))
      ;       [press-end])
      ;      (animated/code!
      ;       (fn []
      ;         (animated/set state (if (true? value) 1 0)))
      ;       [value])
      (reagent/as-element
       [gh/tap-gesture-handler
        (merge tap-handler
               {:shouldCancelWhenOutside true
                :enabled                 (boolean (and onChange (not disabled)))})
        [animated/view
         [component
          {:transition transition
           :hold       hold
           :disabled   disabled}]]]))))

(defn switch-view
  [{:keys [transition hold disabled value]}]
  [animated/view
   {:style               (styles/switch-style transition disabled)
    :accessibility-label (str "switch-" (if value "on" "off"))
    :accessibility-role  :switch}
   [animated/view {:style (styles/switch-bullet-style transition hold)}]])

(defn radio-view
  [{:keys [transition hold disabled value]}]
  [animated/view
   {:style               (styles/radio-style transition disabled)
    :accessibility-label (str "radio-" (if value "on" "off"))
    :accessibility-role  :radio}
   [animated/view {:style (styles/radio-bullet-style transition hold)}]])

(defn checkbox-view
  [props]
  (let [{:keys [value onChange disabled]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-without-feedback
      {:on-press (when (and onChange (not disabled)) onChange)}
      [rn/view
       {:style               (styles/checkbox-style value disabled)
        :accessibility-label (str "checkbox-" (if value "on" "off"))
        :accessibility-role  :checkbox}
       [rn/view {:style (styles/check-icon-style value)}
        [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]]])))

(defn animated-checkbox-view
  [{:keys [transition hold disabled value]}]
  [animated/view
   {:style               (styles/animated-checkbox-style transition disabled)
    :accessibility-label (str "checkbox-" (if value "on" "off"))
    :accessibility-role  :checkbox}
   [animated/view {:style (styles/animated-check-icon-style transition hold)}
    [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]])

(def switch (reagent/adapt-react-class (react/memo (control-builder switch-view))))
(def radio (reagent/adapt-react-class (react/memo (control-builder radio-view))))
(def animated-checkbox
  (reagent/adapt-react-class (react/memo (control-builder animated-checkbox-view))))
(def checkbox (reagent/adapt-react-class (react/memo checkbox-view)))
