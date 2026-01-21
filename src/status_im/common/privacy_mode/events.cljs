(ns status-im.common.privacy-mode.events
  (:require [status-im.common.privacy-mode.view :as privacy-mode]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :privacy-mode/toggle-privacy-mode
 (fn [{:keys [db]} [privacy-mode-enabled?]]
   {:db (assoc db :privacy-mode/privacy-mode-enabled? (not privacy-mode-enabled?))
    :fx [[:dispatch [:hide-bottom-sheet]]]}))

(rf/reg-event-fx :privacy-mode/show-bottom-sheet
 (fn [{:keys [db]} [{:keys [theme shell?]}]]
   {:fx [[:dispatch
          [:show-bottom-sheet
           {:content #(privacy-mode/view (:privacy-mode/privacy-mode-enabled? db))
            :theme   theme
            :shell?  shell?}]]]}))
