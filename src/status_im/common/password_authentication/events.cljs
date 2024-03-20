(ns status-im.common.password-authentication.events
  (:require
    [status-im.navigation.events :as navigation]
    [utils.re-frame :as rf]))

(rf/defn close
  {:events [:password-authentication/show]}
  [{:keys [db]} {:keys [community-id] :as content} button]
  (rf/merge {:db (assoc db
                        :password-authentication
                        {:error  nil
                         :button button})}
            (navigation/show-bottom-sheet content)))
