(ns status-im.protocol.validation)

(defn- fline [and-form] (:line (meta and-form)))

(defmacro valid? [spec x]
  `(let [v?# (cljs.spec/valid? ~spec ~x)]
     (when-not v?#
       (let [explanation# (cljs.spec/explain-str ~spec ~x)]
         (taoensso.timbre/log! :error :p
                               [explanation#]
                               ~{:?line (fline &form)})))
     v?#))
