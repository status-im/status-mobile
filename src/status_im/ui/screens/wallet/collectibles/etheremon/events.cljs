(ns status-im.ui.screens.wallet.collectibles.etheremon.events
  (:require [status-im.ui.screens.wallet.collectibles.events :as collectibles]
            [status-im.utils.http :as http]))

(def emona :EMONA)

(defmethod collectibles/load-collectible-fx emona [_ _ id]
  {:http-get {:url                   (str "https://www.etheremon.com/api/monster/get_data?monster_ids=" id)
              :success-event-creator (fn [o]
                                       [:load-collectible-success emona (:data (http/parse-payload o))])
              :failure-event-creator (fn [o]
                                       [:load-collectible-failure emona {id (http/parse-payload o)}])}})