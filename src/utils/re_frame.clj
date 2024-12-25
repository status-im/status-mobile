(ns utils.re-frame (:refer-clojure :exclude [defn]))

(defn- register-events
  [events interceptors name argsyms]
  (mapv (fn [event]
          `(re-frame.core/reg-event-fx
            ~event
            ~interceptors
            (fn [cofx# [~@argsyms]] (~name cofx# ~@argsyms))))
        events))

(defn- fully-qualified-name
  [sym]
  (str *ns* "/" (name sym)))

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
  - supports attr-map with optional :events key which needs to be a vector of
  event keywords under which the function will be registered
  - TODO: add suport for `prepost-map?` (don't forget to add it to arglist)
  - TODO: add validation of macro parameters"
  {:arglists      '([name doc-string? attr-map? [params*] body])
   :superseded-by "utils.re-frame/reg-event-fx"}
  [name & fdecl]
  (let [m             (if (string? (first fdecl))
                        {:doc (first fdecl)}
                        {})
        fdecl         (if (string? (first fdecl))
                        (next fdecl)
                        fdecl)
        m             (if (map? (first fdecl))
                        (conj m (first fdecl))
                        m)
        events        (get m :events [])
        interceptors  (get m :interceptors [])
        fdecl         (if (map? (first fdecl))
                        (next fdecl)
                        fdecl)
        [cofx & args] (first fdecl)
        fdecl         (next fdecl)
        argsyms       (take (count args) (repeatedly #(gensym "arg")))]
    (if (and (sequential? events)
             (every? keyword? events))
      `(do
         (clojure.core/defn ~(with-meta name m)
           ([~@argsyms] (fn [cofx#] (~(with-meta name m) cofx# ~@argsyms)))
           ([cofx# ~@args]
            (when js/goog.DEBUG
              (when (taoensso.timbre/level>=
                     :trace
                     (:min-level taoensso.timbre/*config*))
                (println
                 (clojure.string/join
                  (concat
                   (repeat
                    (deref utils.re-frame/handler-nesting-level)
                    "│  ")
                   ["├─"]))
                 ~(str (clojure.core/name name) " " *ns*))))
            (if (and (map? cofx#)
                     (not (nil? (:db cofx#))))
              (let [res# (let [~cofx cofx#] ~@fdecl)]
                (when-not (nil? res#)
                  (aset res#
                        "cljs$core$ILookup$_lookup$arity$2"
                        (fn [foo# k#]
                          (clojure.core/this-as
                           m#
                           (when (and (map? k#)
                                      (contains? k# :db))
                             (throw (js/Error. (str
                                                "fx/defn's result is used as fx producing function in "
                                                ~(fully-qualified-name name)))))
                           (get m# k# nil)))))
                res#)
              (throw (js/Error. (str "fx/defn expects a map of cofx as first argument got " cofx#
                                     " in function " ~(fully-qualified-name name)))))))
         ~@(register-events events interceptors (with-meta name m) argsyms))
      (throw
       (Exception.
        (str "fx/defn expects a vector of keyword as value for :events key in attr-map in function "
             name))))))
