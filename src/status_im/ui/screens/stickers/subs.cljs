(ns status-im.ui.screens.stickers.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :stickers/packs
 (fn [db]
   (:stickers/packs db)))

(re-frame/reg-sub
 :stickers/installed-packs
 (fn [db]
   (:stickers/packs-installed db)))

(re-frame/reg-sub
 :stickers/installed-packs-vals
 :<- [:stickers/installed-packs]
 (fn [packs]
   (vals packs)))

(re-frame/reg-sub
 :stickers/all-packs
 :<- [:stickers/packs]
 :<- [:stickers/installed-packs]
 (fn [[packs installed]]
   (map #(if (get installed (:id %)) (assoc % :installed true) %) (vals packs))))

(re-frame/reg-sub
 :stickers/recent
 :<- [:account/account]
 (fn [{:keys [recent-stickers]}]
   recent-stickers))