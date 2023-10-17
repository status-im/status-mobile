(ns status-im2.common.password-authentication.events
  (:require
    [status-im2.navigation.events :as navigation]
    [utils.re-frame :as rf]))

(rf/defn close
  {:events [:password-authentication/show]}
  [{:keys [db]} content button]
  (rf/merge {:db (assoc db
                        :password-authentication
                        {:error  nil
                         :button button})}
            (navigation/show-bottom-sheet content)))
