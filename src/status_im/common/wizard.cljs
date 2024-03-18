(ns status-im.common.wizard
  (:require [utils.re-frame :as rf]))

(defn- wizard-find-next-screen
  [db flow-config current-screen]
  (->> flow-config
       (filter (fn [{:keys [skip-step? screen-id]}]
                 (and (not= screen-id current-screen)
                      (not (and (fn? skip-step?) (skip-step? db))))))
       first))

(rf/reg-event-fx
 :navigation/wizard-forward
 (fn [{:keys [db]} [{:keys [current-screen flow-config start-flow?]}]]
   (let [next-screen (wizard-find-next-screen db flow-config current-screen)]
     {:fx [[:dispatch
            (if start-flow?
              [:open-modal (:screen-id next-screen)]
              [:navigate-to-within-stack [(:screen-id next-screen) current-screen]])]]})))

(rf/reg-event-fx
 :navigation/wizard-backward
 (fn [{:keys [db]}]
   (let [stack          (:modal-view-ids db)
         current-screen (last stack)]
     {:fx [[:dispatch
            (if (= (count stack) 1)
              [:navigate-back]
              [:navigate-back-within-stack current-screen])]]})))