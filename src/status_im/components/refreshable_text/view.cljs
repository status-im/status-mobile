(ns status-im.components.refreshable-text.view
  (:require [reagent.core :as r]
            [reagent.impl.component :as rc]
            [status-im.components.react :refer [view
                                                animated-view
                                                text]]
            [status-im.components.animation :as anim]))

(defn start-animation [{:keys [old-value-top
                               new-value-top
                               old-value-opacity
                               new-value-opacity]}]
  (anim/start
    (anim/timing old-value-top {:toValue  10
                                :duration 300}))
  (anim/start
    (anim/timing new-value-top {:toValue  0
                                :duration 300}))
  (anim/start
    (anim/timing old-value-opacity {:toValue  0
                                    :duration 300}))
  (anim/start
    (anim/timing new-value-opacity {:toValue  1.0
                                    :duration 300})))

(defn refreshable-text [{:keys [value]}]
  (let [old-value-top     (anim/create-value 0)
        new-value-top     (anim/create-value 0)
        old-value-opacity (anim/create-value 0)
        new-value-opacity (anim/create-value 1.0)
        context           {:old-value-top     old-value-top
                           :new-value-top     new-value-top
                           :old-value-opacity old-value-opacity
                           :new-value-opacity new-value-opacity}]
    (r/create-class
      {:get-initial-state
       (fn []
         {:old-value nil
          :value     value})
       :component-will-update
       (fn [component props]
         (let [{new-value :value} (rc/extract-props props)
               {old-value :value} (r/props component)]
           (r/set-state component {:old-value old-value
                                   :value     new-value})
           (anim/set-value old-value-top 0)
           (anim/set-value new-value-top -10)
           (anim/set-value old-value-opacity 1.0)
           (anim/set-value new-value-opacity 0.0)
           (start-animation context)))
       :reagent-render
       (fn [{:keys [style text-style font]}]
         (let [component (r/current-component)
               {:keys [old-value value]} (r/state component)]
           [view style
            [animated-view {:style {:position   :absolute
                                    :margin-top old-value-top
                                    :opacity    old-value-opacity}}
             [text {:style text-style
                    :font  font}
              old-value]]
            [animated-view {:style {:position   :absolute
                                    :margin-top new-value-top
                                    :opacity    new-value-opacity}}
             [text {:style text-style
                    :font  font}
              value]]]))})))
