(ns status-im.common.feature-unavailable-drawer.events
  (:require
    [status-im.common.feature-unavailable-drawer.view :as feature-unavailable]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :feature-unavailable/open-modal
 (fn [_ [options]]
   {:fx [[:dispatch
          [:show-bottom-sheet
           {:content (fn []
                       (feature-unavailable/view options))}]]]}))
