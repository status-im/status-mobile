(ns status-im2.common.async-storage
  (:require [re-frame.core :as re-frame]
            react-native.async-storage))

(re-frame/reg-fx :async-storage-set (react-native.async-storage/set-item-factory))
(re-frame/reg-fx :async-storage-get
                 (fn [{ks :keys cb :cb}] (react-native.async-storage/get-items ks cb)))
