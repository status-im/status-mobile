(ns status-im.utils.slurp
  (:refer-clojure :exclude [slurp])
  (:require [clojure.string :as s]))

(defmacro slurp [file]
  (clojure.core/slurp file))

(defmacro slurp-bot [bot-name & files]
  (->> (concat files ["translations.js" "bot.js"])
       (map (fn [file-name]
              (try
                (clojure.core/slurp
                  (s/join "/" ["resources/js/bots" (name bot-name) file-name]))
                (catch Exception _ ""))))
       (apply str)))
