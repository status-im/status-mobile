(ns status-im.ui.screens.chat.bottom-info
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as styles]))

(defn- container-animation-logic
  [{:keys [to-value val]}]
  (fn [_]
    (anim/start
     (anim/spring val {:toValue  to-value
                       :friction 6
                       :tension  40}))))

(defn overlay
  [{:keys [on-click-outside]} items]
  [react/view styles/bottom-info-overlay
   [react/touchable-highlight {:on-press on-click-outside
                               :style    styles/overlay-highlight}
    [react/view nil]]
   items])

(defn container
  [height & _]
  (let [anim-value    (anim/create-value 1)
        context       {:to-value height
                       :val      anim-value}
        on-update     (container-animation-logic context)]
    (reagent/create-class
     {:component-did-update
      on-update
      :display-name "container"
      :reagent-render
      (fn [height & children]
        [react/animated-view {:style (styles/bottom-info-container height)}
         (into [react/view] children)])})))
