(ns status-im.common.kv-storage.effects
  (:require
    [react-native.mmkv :as mmkv]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.kv/set-object
 (fn [{:keys [id value]}]
   (mmkv/set-object id value)))

(rf/reg-fx :effects.kv/merge-object
 (fn [{key-id :key
       :keys  [value]}]
   (let [kv-object (mmkv/get-object key-id {})]
     (mmkv/set-object key-id
                      (merge kv-object value)))))

(rf/reg-fx :effects.kv/delete-key
 (fn [key-id]
   (mmkv/delete-key key-id)))
