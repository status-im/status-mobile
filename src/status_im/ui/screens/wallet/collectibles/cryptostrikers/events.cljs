(ns status-im.ui.screens.wallet.collectibles.cryptostrikers.events
  (:require [status-im.ui.screens.wallet.collectibles.events :as collectibles]
            [status-im.utils.http :as http]))

(def strikers :STRK)

(defmethod collectibles/load-collectible-fx strikers [_ _ id]
  {:http-get {:url                   (str "https://us-central1-cryptostrikers-prod.cloudfunctions.net/cards/" id)
              :success-event-creator (fn [o]
                                       [:load-collectible-success strikers {id (http/parse-payload o)}])
              :failure-event-creator (fn [o]
                                       [:load-collectible-failure strikers {id (http/parse-payload o)}])}})