(ns status-im.utils.ethereum.macros
  (:require [clojure.string :as string]
            [clojure.java.io :as io]))

(defn icon-path
  [network symbol]
  (let [s (str "./resources/images/tokens/" (name network) "/" (name symbol) ".png")]
    (if (.exists (io/file s))
      `(js/require ~s)
      `(js/require "./resources/images/tokens/default.png"))))

(defn- token->icon [network {:keys [icon symbol]}]
  ;; Tokens can define their own icons.
  ;; If not try to make one using a local image as resource, if it does not exist fallback to default.
  (or icon (icon-path network symbol)))

(defmacro resolve-icons
  "In react-native arguments to require must be static strings.
   Resolve all icons at compilation time so no variable is used."
  [network tokens]
  (mapv #(assoc-in % [:icon :source] (token->icon network %)) tokens))