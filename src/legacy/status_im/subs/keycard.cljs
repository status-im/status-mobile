(ns legacy.status-im.subs.keycard
  (:require
    [legacy.status-im.keycard.common :as common]
    [re-frame.core :as re-frame]
    [utils.datetime :as datetime]))

(re-frame/reg-sub
 :keycard-paired-on
 (fn [db]
   (some-> (get-in db [:profile/profile :keycard-paired-on])
           (datetime/timestamp->year-month-day-date))))

(re-frame/reg-sub
 :keycard-multiaccount?
 common/keycard-multiaccount?)


