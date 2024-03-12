(ns native-module.utils
  (:require
    [clojure.string :as string]
    [promesa.core :as p]
    [utils.transforms :as types]))

(defn- promisify-callback
  [res rej]
  (fn [result]
    (let [error (let [{:keys [error]} (types/json->clj result)]
                  (when-not (string/blank? error)
                    error))]
      (if error (rej error) (res result)))))

(defn promisify-native-module-call
  [func & args]
  (p/create
   (fn [res rej]
     (->> (promisify-callback res rej)
          (conj [])
          (concat args)
          (apply func)))))
