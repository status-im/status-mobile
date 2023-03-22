(ns status-im2.contexts.onboarding.events
  (:require [utils.re-frame :as rf]))

(rf/defn on-delete-profile-success
  {:events [:onboarding/on-delete-profile-success]}
  [{:keys [db]} key-uid]
  {:db (update-in db [:multiaccounts/multiaccounts] dissoc key-uid)})
