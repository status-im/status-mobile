(ns status-im.accounts.model
  (:require [status-im.utils.security :as security]
            [cljs.spec.alpha :as spec]
            [clojure.string :as string]))

(defn logged-in? [cofx]
  (boolean
   (get-in cofx [:db :account/account])))

(defn credentials [cofx]
  (select-keys (get-in cofx [:db :accounts/login]) [:address :password :save-password?]))

(defn account-creation-next-enabled? [{:keys [step password password-confirm name]}]
  (or (and password (= :enter-password step) (spec/valid? :status-im.accounts.db/password (security/safe-unmask-data password)))
      (and password-confirm (= :confirm-password step) (spec/valid? :status-im.accounts.db/password password-confirm))
      (and name (= :enter-name step) (not (string/blank? name)))))

(defn current-public-key
  [cofx]
  (get-in cofx [:db :account/account :public-key]))
