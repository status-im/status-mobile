(ns status-im2.common.password-authentication.events
  (:require [utils.re-frame :as rf]
            [status-im2.navigation.events :as navigation]))

(rf/defn close
  {:events [:password-authentication/show]}
  [{:keys [db]} content button]
  (rf/merge {:db (assoc db
                        :password-authentication
                        {:error  nil
                         :button button})}
            (navigation/show-bottom-sheet content)))
