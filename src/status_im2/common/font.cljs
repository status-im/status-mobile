(ns status-im2.common.font
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    utils.image-server
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 :font/get-font-file-for-initials-avatar
 (fn [callback]
   (utils.image-server/get-font-file-ready callback)))

(rf/defn init-abs-root-path
  {:events [:font/init-font-file-for-initials-avatar]}
  [{:keys [db]} initials-avatar-font-file]
  (when-not (string/blank? initials-avatar-font-file)
    {:db (assoc db :initials-avatar-font-file initials-avatar-font-file)}))
