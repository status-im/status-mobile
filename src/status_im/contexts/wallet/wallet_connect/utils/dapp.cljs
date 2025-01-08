(ns status-im.contexts.wallet.wallet-connect.utils.dapp
  (:require
    [clojure.string :as string]
    utils.string))

(defn compute-name
  "Sometimes dapps have no name or an empty name. Return url as name in that case"
  [name url]
  (if (seq name)
    name
    (when (seq url)
      (-> url
          utils.string/remove-trailing-slash
          utils.string/remove-http-prefix
          string/capitalize))))

(defn compute-icon
  "Some dapps have icons with relative paths, make paths absolute in those cases, send nil if icon is missing"
  [icon-path url]
  (when (and (seq icon-path)
             (seq url))
    (if (string/starts-with? icon-path "http")
      icon-path
      (str (utils.string/remove-trailing-slash url) icon-path))))

(defn best-icon
  "Find the icon format from the provided list that can be rendered (prefer images)"
  [icons]
  (let [image-extensions [".png" ".jpg" ".jpeg"]
        image?           (fn [icon]
                           (some #(string/ends-with? icon %) image-extensions))]
    (or (first (filter image? icons))
        (first icons))))
