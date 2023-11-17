(ns status-im2.util)

(defmacro do-on [event-type & body]
  `(status-im2.util/do-on* ~event-type (fn [] ~@body)))

(defn- run-scenario* [output items]
  (if (empty? items)
    `(do ~@output)
    (let [item (peek items)
          rest (pop items)
          waiting-step? (and (vector? item)
                             (= :on (first item)))]
      (cond
        (not waiting-step?)
        (recur (conj output item) rest)
        ;; ---
        (and waiting-step?
             (seq output)) ;; not empty
        `(do ~@(conj output `(status-im2.util/do-on*
                               ~(second item)
                               (fn [] ~(run-scenario* [] rest)))))
        ;; ---
        :else (throw (Exception. "Illegal state in scenario"))))))

(defmacro run-scenario [& body]
  (run-scenario* [] (apply list body)))
