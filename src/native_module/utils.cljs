(ns native-module.utils
  (:require
    [clojure.string :as string]
    [promesa.core :as promesa]
    [utils.transforms :as types]))

(defn- promisify-callback
  [res rej]
  (fn [result]
    (let [error (let [{:keys [error]} (types/json->clj result)]
                  (when-not (string/blank? error)
                    error))]
      (if error (rej error) (res result)))))

(defn promisify-native-module-call
  [f & args]
  (promesa/create
   (fn [res rej]
     (->> [(promisify-callback res rej)]
          (concat args)
          (apply f)))))
