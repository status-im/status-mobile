(ns status-im.common.serialization
  (:require
    [native-module.core :as native-module]
    [status-im.constants :as constants]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/reg-fx :serialization/deserialize-and-compress-key
 (fn [{:keys [serialized-key on-success on-error]}]
   (native-module/deserialize-and-compress-key
    serialized-key
    (fn [resp]
      (let [{:keys [error]} (transforms/json->clj resp)]
        (if error
          (on-error error)
          (on-success resp)))))))

(rf/reg-fx :serialization/decompress-public-key
 (fn [{:keys [compressed-key on-success on-error]}]
   (native-module/compressed-key->public-key
    compressed-key
    constants/deserialization-key
    (fn [resp]
      (let [{:keys [error]} (transforms/json->clj resp)]
        (if error
          (on-error error)
          (on-success (str "0x" (subs resp 5)))))))))
