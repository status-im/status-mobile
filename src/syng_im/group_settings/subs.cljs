(ns syng-im.group-settings.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub :selected-participant
  (fn [db [_]]
    (reaction
     (let [identity (first (get @db :selected-participants))]
       (get-in @db [:contacts identity])))))

(register-sub :group-settings
  (fn [db [_ k]]
    (reaction (get-in @db [:group-settings k]))))
