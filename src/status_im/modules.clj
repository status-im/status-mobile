(ns status-im.modules
  (:require [clojure.java.io :as io]
            [clojure.tools.reader.edn :as edn])
  (:import (java.io PushbackReader)))

(def params
  (with-open [r (io/reader "params.edn")]
    (edn/read (PushbackReader. r))))

(defn prod-symbols [symbols]
  (mapcat (fn [[k v]]
            [k `(resolve ~v)])
          symbols))

(defn dev-symbols [symbols]
  (mapcat (fn [[k v]]
            [k `(symbol ~(str v))])
          symbols))

(defmacro defmodule [module-name symbols]
  (let [module      (gensym "module")
        loaded?     (gensym "loaded?")
        path        (str "status-modules/cljs/" module-name ".js")
        k           (gensym)
        get-symbol  'get-symbol
        load-module 'load-module]
    (if (= (:env params) :dev)
      `(do
         (def ~module
           (hash-map ~@(dev-symbols symbols)))
         (defn ~get-symbol [~k]
           (get ~module ~k))
         (defn ~load-module []))
      `(do
         (defonce ~loaded? (atom false))
         (defonce ~module (atom {}))
         (defn ~load-module []
           (taoensso.timbre/debug :load-module ~(str module-name))
           (when-not @~loaded?
             (js/eval (js/require ~path))
             (reset! ~module
                     (hash-map ~@(prod-symbols symbols)))
             (reset! ~loaded? true))
           (taoensso.timbre/debug :module-loaded ~(str module-name)))
         (defn ~get-symbol [~k]
           (~load-module)
           (get @~module ~k))))))
