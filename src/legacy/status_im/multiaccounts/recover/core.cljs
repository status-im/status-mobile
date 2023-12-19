(ns legacy.status-im.multiaccounts.recover.core
  (:require
    [legacy.status-im.multiaccounts.create.core :as multiaccounts.create]
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [taoensso.timbre :as log]))

(re-frame/reg-fx
 ::import-multiaccount
 (fn [{:keys [passphrase password success-event]}]
   (log/debug "[recover] ::import-multiaccount")
   (native-module/multiaccount-import-mnemonic
    passphrase
    password
    (fn [result]
      (let [{:keys [id] :as root-data}
            (multiaccounts.create/normalize-multiaccount-data-keys
             (types/json->clj result))]
        (native-module.core/multiaccount-derive-addresses
         id
         [constants/path-wallet-root
          constants/path-eip1581
          constants/path-whisper
          constants/path-default-wallet]
         (fn [result]
           (let [derived-data (multiaccounts.create/normalize-derived-data-keys
                               (types/json->clj result))]
             (re-frame/dispatch [success-event root-data derived-data])))))))))

