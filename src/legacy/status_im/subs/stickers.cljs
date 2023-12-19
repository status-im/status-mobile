(ns legacy.status-im.subs.stickers
  (:require
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]))

(re-frame/reg-sub
 :stickers/installed-packs
 :<- [:stickers/packs]
 (fn [packs]
   (filter #(= (:status %) constants/sticker-pack-status-installed) (vals packs))))

(re-frame/reg-sub
 :stickers/all-packs
 :<- [:stickers/packs]
 (fn [packs]
   (map (fn [{:keys [status] :as pack}]
          (-> pack
              (assoc :installed (= status constants/sticker-pack-status-installed))
              (assoc :pending (= status constants/sticker-pack-status-pending))
              (assoc :owned (= status constants/sticker-pack-status-owned))))
        (vals packs))))

(re-frame/reg-sub
 :stickers/get-current-pack
 :<- [:get-screen-params]
 :<- [:stickers/all-packs]
 (fn [[{:keys [id]} packs]]
   (first (filter #(= (:id %) id) packs))))
