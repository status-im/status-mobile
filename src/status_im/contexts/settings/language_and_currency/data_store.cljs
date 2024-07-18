
(ns status-im.contexts.settings.language-and-currency.data-store
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [utils.i18n :as i18n]))

(defn rpc->currency
  [currency]
  (-> currency
      (set/rename-keys
       {:shortName :code
        :name      :display-name
        :isPopular :popular?
        :isToken   :token?})
      (update :id keyword)
      (dissoc :imageSource)))

(defn rpc->currencies
  [currencies]
  (map rpc->currency currencies))

(defn get-formatted-currency-data
  [{:keys [popular crypto other]}]
  (concat
   (when-not (empty? popular)
     [{:title (i18n/label :t/popular-currencies)
       :data  popular}])
   (when-not (empty? crypto)
     [{:title (i18n/label :t/crypto)
       :data  crypto}])
   (when-not (empty? other)
     [{:title (i18n/label :t/all-currencies)
       :data  other}])))
