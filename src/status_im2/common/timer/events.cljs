(ns status-im2.common.timer.events
  (:require
    [re-frame.core :as re-frame]
    [react-native.background-timer :as background-timer]))

(re-frame/reg-fx
 :background-timer/dispatch-later
 (fn [params]
   (doseq [{:keys [ms dispatch]} params]
     (when (and ms dispatch)
       (background-timer/set-timeout #(re-frame/dispatch dispatch) ms)))))

(re-frame/reg-fx
 :background-timer/clear-timeouts
 (fn [ids]
   (doseq [id ids]
     (when id
       (background-timer/clear-timeout id)))))
