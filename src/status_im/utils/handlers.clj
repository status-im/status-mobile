(ns status-im.utils.handlers)

(defmacro handlers->
  "Help thread multiple handler functions.
   All functions are expected to accept [db event] as parameters.
   If one handler returns a modified db it will be used as parameters for subsequent handlers."
  [& forms]
  (let [db     (gensym "db")
        event  (gensym "event")
        new-db (gensym "new-db")]
    `(fn [~db ~event]
       (let [~@(interleave (repeat db)
                           (map (fn [form]
                                  `(let [~new-db (~form ~db ~event)]
                                     (if (map? ~new-db)
                                       ~new-db
                                       ~db))) forms))]
         ~db))))
