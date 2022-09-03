(ns status-im.ethereum.macros
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(defn token-icon-path [path]
  (fn [el]
    (let [el    (cstr/replace el ".png" "")
          s     (str path el ".png")
          s-js  (str "." s)]
      (when (.exists (io/file s))
        [el `(js/require ~s-js)]))))

(defmacro resolve-icons
  "In react-native arguments to require must be static strings.
   Resolve all icons at compilation time so no variable is used."
  [network]
  (let [path (str "./resources/images/tokens/" (name network) "/")
        files (->> (io/file path)
                   file-seq
                   (filter #(cstr/ends-with? % "png"))
                   (map #(first (cstr/split (.getName %) #"@")))
                   distinct)]
    (into {} (map (token-icon-path path) files))))

(defn network->icon [network]
  (let [s     (str "./resources/images/tokens/" (name network) "/0-native.png")
        s-js  (str "." s)]
    (if (.exists (io/file s))
      `(js/require ~s-js)
      `(js/require "../resources/images/tokens/default-token.png"))))

(defmacro resolve-native-currency-icons
  "In react-native arguments to require must be static strings.
   Resolve all icons at compilation time so no variable is used."
  [all-native-currencies]
  (into {}
        (map (fn [[network native-currency]]
               [network (assoc-in native-currency
                                  [:icon :source]
                                  (network->icon network))]) all-native-currencies)))
