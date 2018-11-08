(ns status-im.ui.screens.chat.bottom-info
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.animation :as anim]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.screens.chat.styles.main :as styles]
            [status-im.utils.core :as utils]))

(defn- container-animation-logic [{:keys [to-value val]}]
  (fn [_]
    (anim/start
     (anim/spring val {:toValue  to-value
                       :friction 6
                       :tension  40}))))

(defn overlay [{:keys [on-click-outside]} items]
  [react/view styles/bottom-info-overlay
   [react/touchable-highlight {:on-press on-click-outside
                               :style    styles/overlay-highlight}
    [react/view nil]]
   items])

(defn container [height & _]
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

(defn- message-status-row
  [{:keys [photo-path name public-key status]}]
  [react/view styles/bottom-info-row
   [photos/photo photo-path
    styles/bottom-info-row-photo-size]
   [react/view styles/bottom-info-row-text-container
    [react/text {:style           styles/bottom-info-row-text1
                 :number-of-lines 1}
     (utils/truncate-str name 30)]
    [react/text {:style           styles/bottom-info-row-text2
                 :number-of-lines 1}
     (i18n/message-status-label (or status :sending))]]])

(defn bottom-info-view [message-details]
  (reagent/create-class
   {:display-name "bottom-info-view"
    :reagent-render
    (fn [{:keys [participants participants-count]}]
      [overlay {:on-click-outside #(re-frame/dispatch [:chat.ui/set-chat-ui-props {:message-id nil}])}
       [container (* styles/item-height participants-count)
        [list/flat-list {:contentContainerStyle styles/bottom-info-list-container
                         :data                participants
                         :key-fn              :address
                         :render-fn           message-status-row
                         :enableEmptySections true}]]])}))
