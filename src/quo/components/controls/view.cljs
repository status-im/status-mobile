(ns quo.components.controls.view
  (:require [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo.animated :as animated]
            [quo.gesture-handler :as gh]
            [quo.design-system.colors :as colors]
            [quo.components.controls.styles :as styles]
            [status-im.ui.components.icons.icons :as icons]))

(defn control-builder [component]
  (fn [props]
    (let [{:keys [value onChange disabled]}
          (bean/bean props)
          state       (animated/use-value 0)
          tap-state   (animated/use-value (:undetermined gh/states))
          tap-handler (animated/on-gesture {:state tap-state})
          hold        (react/use-memo
                       (fn []
                         (animated/with-timing-transition
                           (animated/eq tap-state (:began gh/states))
                           {}))
                       [])
          transition  (react/use-memo
                       (fn []
                         (animated/with-spring-transition state (:lazy animated/springs)))
                       [])
          press-end   (fn []
                        (when (and (not disabled) onChange)
                          (onChange (not value))))]
      (animated/code!
       (fn []
         (animated/cond* (animated/eq tap-state (:end gh/states))
                         [(animated/set state (animated/not* state))
                          (animated/set tap-state (:undetermined gh/states))
                          (animated/call* [] press-end)]))
       [press-end])
      (animated/code!
       (fn []
         (animated/set state (if (true? value) 1 0)))
       [value])
      (reagent/as-element
       [gh/tap-gesture-handler (merge tap-handler
                                      {:shouldCancelWhenOutside true
                                       :enabled                 (boolean (and onChange (not disabled)))})
        [animated/view
         [component {:transition transition
                     :hold       hold
                     :disabled   disabled}]]]))))

(defn switch-view [{:keys [transition hold disabled]}]
  [animated/view {:style               (styles/switch-style transition disabled)
                  :accessibility-label :switch
                  :accessibility-role  :switch}
   [animated/view {:style (styles/switch-bullet-style transition hold)}]])

(defn radio-view [{:keys [transition hold disabled]}]
  [animated/view {:style (styles/radio-style transition disabled)
                  :accessibility-label :radio
                  :accessibility-role  :radio}
   [animated/view {:style (styles/radio-bullet-style transition hold)}]])

(defn checkbox-view [props]
  (let [{:keys [value onChange disabled]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-highlight
      {:on-press (when onChange onChange)}
      [rn/view {:style               (styles/checkbox-style value disabled)
                :accessibility-label :checkbox
                :accessibility-role  :checkbox}
       [rn/view {:style (styles/check-icon-style value)}
        [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]]])))

(defn animated-checkbox-view [{:keys [transition hold disabled]}]
  [animated/view
   {:style               (styles/animated-checkbox-style transition disabled)
    :accessibility-label :checkbox
    :accessibility-role  :checkbox}
   [animated/view {:style (styles/animated-check-icon-style transition hold)}
    [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]])

(def switch (reagent/adapt-react-class (react/memo (control-builder switch-view))))
(def radio (reagent/adapt-react-class (react/memo (control-builder radio-view))))
(def animated-checkbox
  (reagent/adapt-react-class (react/memo (control-builder animated-checkbox-view))))
(def checkbox (reagent/adapt-react-class (react/memo checkbox-view)))
