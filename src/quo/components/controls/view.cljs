(ns quo.components.controls.view
  (:require [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.react :as react]
            [quo.platform :as platform]
            [quo.animated :as animated]
            [quo.gesture-handler :as gh]
            [quo.design-system.colors :as colors]
            [quo.components.controls.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn control-builder [component]
  (fn [props]
    (let [{:keys [value onChange disabled animated]}
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
          press-end   (react/callback
                       (fn []
                         (when (and (not disabled) onChange)
                           (onChange (not value))))
                       [onChange disabled])
          pressable   (cond
                        (not onChange) animated/view
                        animated       gh/tap-gesture-handler
                        :else          gh/touchable-highlight)]
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
       [pressable (merge tap-handler
                         {:shouldCancelWhenOutside true
                          :enabled                 (boolean (and onChange (not disabled)))})
        [animated/view
         [component {:transition transition
                     :hold       hold}]]]))))

(defn switch-view [{:keys [transition hold]}]
  [animated/view {:style               (styles/switch-style transition)
                  :accessibility-label :switch
                  :accessibility-role  :switch}
   [animated/view {:style (styles/switch-bullet-style transition hold)}]])

(defn radio-view [{:keys [transition hold]}]
  [animated/view {:style (styles/radio-style transition)
                  :accessibility-label :radio
                  :accessibility-role  :radio}
   [animated/view {:style (styles/radio-bullet-style transition hold)}]])

(defn checkbox-view [{:keys [transition hold]}]
  [animated/view {:style               (styles/checkbox-style transition)
                  :accessibility-label :checkbox
                  :accessibility-role  :checkbox}
   [animated/view {:style (styles/check-icon-style transition hold)}
    [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]])

(def switch (reagent/adapt-react-class (react/memo (control-builder switch-view))))
(def radio (reagent/adapt-react-class (react/memo (control-builder radio-view))))
(def checkbox (reagent/adapt-react-class (react/memo (control-builder checkbox-view))))
