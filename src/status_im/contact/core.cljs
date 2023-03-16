(ns status-im.contact.core
  (:require [utils.re-frame :as rf]
            [status-im2.navigation.events :as navigation]))

(rf/defn open-contact-toggle-list
  {:events [:contact.ui/start-group-chat-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name           "")}
            (navigation/navigate-to-cofx :contact-toggle-list nil)))
