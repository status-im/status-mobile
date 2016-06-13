(ns status-im.login.handlers
  (:require [re-frame.core :refer [register-handler after dispatch]]
            [status-im.utils.handlers :as u]))


(defn set-login-from-qr
  [{:keys [login] :as db} [_ _ login-info]]
  (assoc db :login (merge login login-info)))

(register-handler :set-login-from-qr set-login-from-qr)


(defn go-back
  [_ _]
  (dispatch [:navigate-back]))

(defn login
  [{:keys [login] :as db} [_ _]]
   (assoc db :current-account {:address (:address login)}))

(register-handler :login-account
  (-> login
      ((after go-back))))