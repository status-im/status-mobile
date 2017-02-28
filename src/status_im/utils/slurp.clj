(ns status-im.utils.slurp
  (:refer-clojure :exclude [slurp])
  (:require [clojure.string :as s]))

(defmacro slurp [file]
  (clojure.core/slurp file))

(defmacro slurp-bot [bot-name & files]
  (->> (concat files ["translations.js" "bot.js"])
       (map #(clojure.core/slurp (s/join "/" ["bots" (name bot-name) %])))
       (apply str)))
