(ns status-im.common.feature-unavailable.events
  (:require
    [status-im.common.feature-unavailable.view :as feature-unavailable]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :keycard/feature-unavailable-show
 (fn [_]
   {:fx [[:dispatch [:show-bottom-sheet {:content feature-unavailable/view}]]]}))
