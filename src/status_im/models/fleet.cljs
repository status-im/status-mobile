(ns status-im.models.fleet
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [status-im.utils.types :as types]
            [status-im.constants :as constants]
            [status-im.utils.config :as config]))

(defn current-fleet
  ([db]
   (current-fleet db nil))
  ([db address]
   (keyword (or (if address
                  (get-in db [:accounts/accounts address :settings :fleet])
                  (get-in db [:account/account :settings :fleet]))
                config/fleet))))

(def fleets
  (:fleets (types/json->clj (slurp "resources/config/fleets.json"))))

(defn format-wnode
  [wnode address]
  {:id wnode
   :name (name wnode)
   :password constants/inbox-password
   :address address})

(defn format-wnodes
  [wnodes]
  (reduce (fn [acc [wnode address]]
            (assoc acc wnode (format-wnode wnode address)))
          {}
          wnodes))

(def default-wnodes
  (reduce (fn [acc [fleet node-by-type]]
            (assoc acc fleet (format-wnodes (:mail node-by-type))))
          {}
          fleets))
