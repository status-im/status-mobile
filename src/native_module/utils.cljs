(ns native-module.utils
  (:require
    [clojure.string :as string]
    [promesa.core :as promesa]
    [utils.transforms :as types]))

(defn- promisify-callback
  [res rej]
  (fn [result]
    (let [native-error (let [{:keys [error]} (types/json->clj result)]
                         (when-not (string/blank? error)
                           error))]
      (if native-error
        (rej (ex-info "Native module call error" {:error native-error}))
        (res result)))))

(defn promisify-native-module-call
  [f & args]
  (promesa/create
   (fn [res rej]
     (->> [(promisify-callback res rej)]
          (concat args)
          (apply f)))))
