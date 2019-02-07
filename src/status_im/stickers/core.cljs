(ns status-im.stickers.core
  (:require [status-im.utils.fx :as fx]
            [cljs.reader :as edn]
            [status-im.accounts.core :as accounts]
            [status-im.ui.screens.navigation :as navigation]))

(fx/defn init-stickers-packs [{:keys [db]}]
  (let [sticker-packs (map edn/read-string (get-in db [:account/account :stickers]))]
    {:db (assoc db :stickers/packs-installed (into {} (map #(vector (:id %) %) sticker-packs)))}))

(fx/defn install-stickers-pack [{{:account/keys [account] :as db} :db :as cofx} id]
  (let [pack (get-in db [:stickers/packs id])]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:stickers/packs-installed id] pack)
              (assoc :stickers/selected-pack id))}
     (accounts/update-stickers (conj (:stickers account) (pr-str pack))))))

(fx/defn load-sticker-pack-success [{:keys [db] :as cofx} edn-string uri open?]
  (let [{{:keys [id] :as pack} 'meta} (edn/read-string edn-string)
        pack' (assoc pack :uri uri)]
    (fx/merge cofx
              {:db (-> db (assoc-in [:stickers/packs id] pack'))}
              #(when open? (navigation/navigate-to-cofx % :stickers-pack-modal pack')))))

(defn find-pack [pack-uri]
  (fn [{:keys [uri] :as pack}]
    (when (= pack-uri uri)
      pack)))

(fx/defn open-sticker-pack [{{:stickers/keys [packs packs-installed]} :db :as cofx} uri]
  (when uri
    (let [pack (some (find-pack uri) (concat (vals packs-installed) (vals packs)))]
      (if pack
        (navigation/navigate-to-cofx cofx :stickers-pack-modal pack)
        {:http-get {:url                   uri
                    :success-event-creator (fn [o]
                                             [:stickers/load-sticker-pack-success o uri true])
                    :failure-event-creator (fn [o] nil)}}))))