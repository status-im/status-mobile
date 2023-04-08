(ns status-im2.contexts.chat.home.events
  (:require
    [utils.re-frame :as rf]))

(rf/defn messages-home-select-tab-event
  {:events [:messages-home-view/select-tab]}
  [{:keys [db]} tab]
  {:db (assoc db :messages-home-view/selected-tab tab)})
