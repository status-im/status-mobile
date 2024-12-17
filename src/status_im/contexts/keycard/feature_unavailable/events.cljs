(ns status-im.contexts.keycard.feature-unavailable.events
  (:require
    [status-im.constants :as constants]
    [status-im.contexts.keycard.feature-unavailable.view :as feature-unavailable]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :keycard/feature-unavailable-upvote
 (fn [_ [{:keys [feature-name]}]]
   {:fx [[:dispatch [:open-url constants/mobile-upvote-link]]
         [:dispatch
          [:centralized-metrics/track
           :metric/feature-unavailable-upvote
           {:feature-name feature-name}]]]}))

(rf/reg-event-fx
 :keycard/feature-unavailable-show
 (fn [_ [{:keys [feature-name theme] :as options}]]
   {:fx [[:dispatch
          [:show-bottom-sheet
           {:theme   theme
            :content (fn []
                       (feature-unavailable/view options))}]]
         [:dispatch
          [:centralized-metrics/track
           :metric/feature-unavailable
           {:feature-name feature-name}]]]}))
