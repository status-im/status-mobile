(ns status-im2.common.bottom-sheet.events
  (:require [utils.re-frame :as rf]
            [status-im2.constants :as constants]
            [react-native.background-timer :as timer]))

(rf/defn show-bottom-sheet
  [{:keys [db]} {:keys [view options]}]
  {:display-bottom-sheet-overlay nil
   :db                   (assoc db
                                :bottom-sheet/show?   true
                                :bottom-sheet/view    view
                                :bottom-sheet/options options)})

(rf/defn show-bottom-sheet-event
  {:events [:bottom-sheet/show-sheet]}
  [cofx view options]
  (show-bottom-sheet
   cofx
   {:view    view
    :options options}))

(rf/defn reset-bottom-sheet
  {:events [:bottom-sheet/reset]}
  [{:keys [db]}]
  {:db                        (assoc db
                                     :bottom-sheet/config
                                     {:content-height      nil
                                      :show-bottom-sheet?  nil
                                      :keyboard-was-shown? false
                                      :expanded?           false
                                      :gesture-running?    false
                                      :animation-delay     constants/bottom-sheet-animation-delay}
                                     :bottom-sheet/show?  false)
   :hide-bottom-sheet-overlay nil})

(rf/defn hide-bottom-sheet
  {:events [:bottom-sheet/hide]}
  [{:keys [db]}]
  {:dispatch       [:bottom-sheet/update-config
                    {:config :show-bottom-sheet?
                     :value  false}]
   :dispatch-later [{:dispatch [:bottom-sheet/reset]
                     :ms       constants/bottom-sheet-animation-delay}]})

(rf/defn hide-bottom-sheet-and-dispatch
  {:events [:bottom-sheet/hide-and-dispatch]}
  [{:keys [db]} on-closing-animation-finished]
  (timer/set-timeout #(when (fn? on-closing-animation-finished)
                        (on-closing-animation-finished))
                     (+ 50 constants/bottom-sheet-animation-delay))
  {:dispatch [:bottom-sheet/hide]})

(rf/defn update-bottom-sheet-config
  {:events [:bottom-sheet/update-config]}
  [{:keys [db]} {:keys [config value]}]
  {:db (assoc-in db [:bottom-sheet/config config] value)})