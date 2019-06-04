(ns status-im.modules)

(def prod? (= "prod" (System/getenv "BUILD_ENV")))

(defn prepare-symbols [symbols]
  (mapcat (fn [[k v]]
            [k `(resolve ~v)])
          symbols))

(defn require-namespaces [symbols]
  (distinct
   (map (fn [[_ [_ v]]]
          `(quote ~(symbol (namespace v))))
        symbols)))

(defmacro defmodule [module-name symbols]
  (let [module      (gensym "module")
        loaded?     (gensym "loaded?")
        path        (str "status-modules/cljs/" module-name ".js")
        k           (gensym)
        get-symbol  'get-symbol
        load-module 'load-module]
    (if prod?
      `(do
         (defonce ~loaded? (atom false))
         (defonce ~module (atom {}))
         (defn ~load-module []
           (when-not @~loaded?
             (js/eval (js/require ~path))
             (reset! ~module
                     (hash-map ~@(prepare-symbols symbols)))
             (reset! ~loaded? true)))
         (defn ~get-symbol [~k]
           (~load-module)
           (get @~module ~k)))
      `(do
         (require ~@(require-namespaces symbols))
         (defonce ~loaded? (atom false))
         (defonce ~module (atom {}))
         (defn ~load-module []
           (when-not @~loaded?
             (reset! ~module
                     (hash-map ~@(prepare-symbols symbols)))
             (reset! ~loaded? true)))
         (defn ~get-symbol [~k]
           (~load-module)
           (get @~module ~k))))))
