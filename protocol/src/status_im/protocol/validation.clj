(ns status-im.protocol.validation)

(defn- fline [and-form] (:line (meta and-form)))

(defmacro valid? [spec x]
  `(let [v?# (cljs.spec.alpha/valid? ~spec ~x)]
     (when-not v?#
       (let [explanation# (cljs.spec.alpha/explain-str ~spec ~x)]
         (taoensso.timbre/log! :error :p
                               [explanation#]
                               ~{:?line (fline &form)})))
     v?#))
