(ns status-im.common.async-storage.effects
  (:require
    [react-native.async-storage :as async-storage]
    [utils.re-frame :as rf]))

(rf/reg-fx :effects.async-storage/set
 (async-storage/set-item-factory))

(rf/reg-fx :effects.async-storage/get
 (fn [{ks :keys cb :cb}]
   (async-storage/get-items ks cb)))
