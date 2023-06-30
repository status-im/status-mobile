(ns status-im.multiaccounts.key-storage.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [native-module.core :as native-module]
            [status-im.utils.types :as types]
            [utils.security.core :as security]))

(re-frame/reg-fx
 :key-storage/delete-imported-key
 (fn [{:keys [key-uid address password on-success on-error]}]
   (let [hashed-pass (ethereum/sha3 (security/safe-unmask-data password))]
     (native-module/delete-imported-key
      key-uid
      (string/lower-case (subs address 2))
      hashed-pass
      (fn [result]
        (let [{:keys [error]} (types/json->clj result)]
          (if-not (string/blank? error)
            (on-error error)
            (on-success))))))))
