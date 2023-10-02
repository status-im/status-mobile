(ns status-im2.contexts.communities.home.events
  (:require
    [utils.re-frame :as rf]))

(rf/defn communities-select-tab-event
  {:events [:communities/select-tab]}
  [{:keys [db]} tab]
  {:db (assoc db :communities/selected-tab tab)})
