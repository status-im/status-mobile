(ns status-im.utils.slurp
  (:refer-clojure :exclude [slurp])
  (:require [clojure.string :as string]
            [hickory.core :as hickory]))

(defmacro slurp [file]
  (clojure.core/slurp file))

(defmacro slurp-bot [bot-name & files]
  (->> (concat files ["translations.js" "bot.js"])
       (map (fn [file-name]
              (try
                (clojure.core/slurp
                  (string/join "/" ["resources/js/bots" (name bot-name) file-name]))
                (catch Exception _ ""))))
       (apply str)))

(def svg-tags #{:g :rect :path :use :defs})

(defmacro slurp-svg [file]
  "Reads svg file, and return function (fn [color] ..), which returns hiccup structure for react-native-svg lib

  Example

  SVG:
  <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"25\" height=\"25\" viewBox=\"0 0 25 25\">\n
  <path fill=\"\" fill-rule=\"evenodd\" d=\"M13.5416667,11.4583333 L13.5416667,6.2571307 C13.5416667,5.67702996 13.0752966,5.20833333 12.5,5.20833333 C11.9206925,5.20833333 11.4583333,5.67789591 11.4583333,6.2571307 L11.4583333,11.4583333 L6.2571307,11.4583333 C5.67702996,11.4583333 5.20833333,11.9247034 5.20833333,12.5 C5.20833333,13.0793075 5.67789591,13.5416667 6.2571307,13.5416667 L11.4583333,13.5416667 L11.4583333,18.7428693 C11.4583333,19.32297 11.9247034,19.7916667 12.5,19.7916667 C13.0793075,19.7916667 13.5416667,19.3221041 13.5416667,18.7428693 L13.5416667,13.5416667 L18.7428693,13.5416667 C19.32297,13.5416667 19.7916667,13.0752966 19.7916667,12.5 C19.7916667,11.9206925 19.3221041,11.4583333 18.7428693,11.4583333 L13.5416667,11.4583333 Z\"/>\n
  </svg>

  Result:
  (fn [color]
    [path {:fill color :fill-rule \"evenodd\" :d \"M13.5416667...\"}]

  Attention!!!: Please make sure svg file has fill field, and has structure like above
  "
  (let [svg (-> (clojure.core/slurp file)
                (string/replace #"[\n]\s*" ""))
        svg-hiccup (first (map hickory/as-hiccup (hickory/parse-fragment svg)))
        color (gensym "args")]
    `(fn [~color]
       ~(into []
          (clojure.walk/prewalk
            (fn [node]
              (if (svg-tags node)
                (if (= :use node)
                  (symbol "use-def")
                  (symbol (name node)))
                (if (vector? node)
                  (let [[k v] node]
                    (if (and (= :fill k) v)
                      [k color]
                      node))
                  node)))
            (rest (rest svg-hiccup)))))))