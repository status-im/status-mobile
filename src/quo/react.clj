(ns quo.react)

(defmacro maybe-js-deps [deps]
  `(if ~deps (into-array ~deps) js/undefined))

(defmacro with-deps-check [[prev-deps] f deps]
  `(let [~prev-deps (quo.react/ref ~deps)]
     (when (not= @~prev-deps ~deps)
       (reset! ~prev-deps ~deps))
     ~f))

(defmacro with-effect
  "Takes optional vector of dependencies and body to be executed in an effect."
  [deps & body]
  (let [[deps setup-fn] (if (vector? deps)
                          [deps body]
                          [nil (cons deps body)])]
    `(effect! #(do ~@setup-fn) ~deps)))

(defmacro with-layout-effect
  "Takes optional vector of dependencies and body to be executed in a layout effect."
  [deps & body]
  (let [[deps setup-fn] (if (vector? deps)
                          [deps body]
                          [nil (cons deps body)])]
    `(layout-effect! #(do ~@setup-fn) ~deps)))


