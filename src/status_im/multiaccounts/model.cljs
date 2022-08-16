(ns status-im.multiaccounts.model)

(defn logged-in? [cofx]
  (boolean
   (get-in cofx [:db :multiaccount])))

(defn credentials [cofx]
  (select-keys (get-in cofx [:db :multiaccounts/login]) [:key-uid :password :save-password?]))

(defn current-public-key
  [cofx]
  (get-in cofx [:db :multiaccount :public-key]))
