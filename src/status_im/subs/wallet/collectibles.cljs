(ns status-im.subs.wallet.collectibles
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]))

(defn- preview-url
  [{:keys [image-url animation-url animation-media-type]}]
  (if (and (not (string/blank? animation-url))
           (not= animation-media-type "image/svg+xml"))
    animation-url
    image-url))

(re-frame/reg-sub
 :wallet/collectibles
 :<- [:wallet]
 (fn [wallet]
   (map (fn [collectible]
          (assoc collectible :preview-url (preview-url (:collectible-data collectible))))
        (:collectibles wallet))))

(re-frame/reg-sub
 :wallet/last-collectible-details
 :<- [:wallet]
 (fn [wallet]
   (let [last-collectible (:last-collectible-details wallet)]
     (assoc last-collectible :preview-url (preview-url (:collectible-data last-collectible))))))

(re-frame/reg-sub
 :wallet/last-collectible-chain-id
 :<- [:wallet/last-collectible-details]
 (fn [collectible]
   (get-in collectible [:id :contract-id :chain-id])))
