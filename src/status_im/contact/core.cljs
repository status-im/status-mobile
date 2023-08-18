(ns status-im.contact.core
  (:require [status-im2.navigation.events :as navigation]
            [utils.re-frame :as rf]))

(rf/defn open-contact-toggle-list
  {:events [:contact.ui/start-group-chat-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name           "")}
            (navigation/navigate-to :contact-toggle-list nil)))

(defn displayed-photo
  [{:keys [images]}]
  (or (:large images)
      (:thumbnail images)
      (first images)))
