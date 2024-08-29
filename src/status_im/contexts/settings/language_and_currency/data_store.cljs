(ns status-im.contexts.settings.language-and-currency.data-store
  (:require [clojure.set :as set]
            [utils.i18n :as i18n]))

(defn rpc->currency
  [currency]
  (some-> currency
          (dissoc :unicode)
          (set/rename-keys
           {:shortName :short-name
            :isPopular :popular?
            :isToken   :token?})
          (update :id keyword)))

(defn rpc->currencies
  [currencies]
  (map rpc->currency currencies))

(defn get-formatted-currency-data
  [{:keys [popular crypto other]}]
  (concat
   (when (seq popular)
     [{:title (i18n/label :t/popular-currencies)
       :data  popular}])
   (when (seq crypto)
     [{:title (i18n/label :t/crypto)
       :data  crypto}])
   (when (seq other)
     [{:title (i18n/label :t/all-currencies)
       :data  other}])))
