(ns status-im.bottom-sheet.core
  (:require [utils.re-frame :as rf]
            [status-im.constants :as constants]))

(rf/defn show-bottom-sheet
  [{:keys [db]} {:keys [view options]}]
  {:show-bottom-sheet nil
   :db                (assoc db
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
  [{:keys [db]} on-cancel]
  {:db (assoc db
              :bottom-sheet/config
              {:content-height      nil
               :show-bottom-sheet?  nil
               :keyboard-was-shown? false
               :expanded?           false
               :gesture-running?    false
               :animation-delay     constants/bottom-sheet-animation-delay})})

(rf/defn hide-bottom-sheet
  {:events [:bottom-sheet/hide]}
  [{:keys [db]}]
  {:dispatch       [:bottom-sheet/update-config
                    {:config :show-bottom-sheet?
                     :value  false}]
   :dispatch-later [{:dispatch [:bottom-sheet/reset nil]
                     :ms       constants/bottom-sheet-animation-delay}]})

(rf/defn hide-bottom-sheet-and-dispatch
  {:events [:bottom-sheet/hide-and-dispatch]}
  [{:keys [db]} on-cancel]
  (when (fn? on-cancel) (on-cancel))
  {:bottom-sheet/hide nil})
