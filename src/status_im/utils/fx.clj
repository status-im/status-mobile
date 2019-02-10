(ns status-im.utils.fx
  (:refer-clojure :exclude [defn]))

(defmacro defn
  "Defines an fx producing function
  Takes the same arguments as the defn macro
  Produces a 2 arity function:
  - first arity takes the declared parameters and returns a function that takes cofx as
  single argument, for use in composition of effects
  - second arity takes cofx as first arguments and declared parameters as next arguments,
  for use in repl or direct call
  Notes:
  - destructuring of cofx is possible
  - supports docstring
  - supports attr-map
  - TODO: add suport for `prepost-map?` (don't forget to add it to arglist)
  - TODO: add validation of macro parameters"
  {:arglists '([name doc-string? attr-map? [params*] body])}
  [name & fdecl]
  (let [m (if (string? (first fdecl))
            {:doc (first fdecl)}
            {})
        fdecl (if (string? (first fdecl))
                (next fdecl)
                fdecl)
        m (if (map? (first fdecl))
            (conj m (first fdecl))
            m)
        fdecl (if (map? (first fdecl))
                (next fdecl)
                fdecl)
        [cofx & args] (first fdecl)
        fdecl (next fdecl)
        argsyms (take (count args) (repeatedly #(gensym "arg")))]
    `(clojure.core/defn ~(with-meta name m)
       ([~@argsyms] (fn [cofx#] (~name cofx# ~@argsyms)))
       ([cofx# ~@args]
        (if (and (map? cofx#)
                 (not (nil? (:db cofx#))))
          (let [~cofx cofx#]
            ~@fdecl)
          (throw (js/Error. (str "fx/defn expects a map of cofx as first argument got " cofx# " in function " ~name))))))))
