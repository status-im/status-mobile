(ns status-im.stickers.core
  (:require [status-im.utils.fx :as fx]
            [cljs.reader :as edn]
            [status-im.accounts.core :as accounts]))

(fx/defn init-stickers-packs [{:keys [db]}]
  (let [sticker-packs (map #(get (edn/read-string %) 'meta) (get-in db [:account/account :stickers]))]
    {:db (assoc db :stickers/packs-installed (into {} (map #(vector (:id %) %) sticker-packs)))}))

(fx/defn install-stickers-pack [{{:account/keys [account] :as db} :db :as cofx} id]
  (let [pack (get-in db [:stickers/packs id])]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:stickers/packs-installed id] pack)
              (assoc :stickers/selected-pack id))}
     (accounts/update-stickers (conj (:stickers account) (:edn pack))))))