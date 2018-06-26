(ns status-im.models.account)

(defn logged-in? [cofx]
  (boolean
   (get-in cofx [:db :account/account])))
