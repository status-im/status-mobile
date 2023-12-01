(ns utils.scenario
  (:require [clojure.set :as set]))

(defmacro do-on-event [event & body]
  `(utils.scenario/do-on-event* ~event (fn [] ~@body)))

(defmacro do-on-event-name [event-name & body]
  `(utils.scenario/do-on-event-name* ~event-name (fn [] ~@body)))

(defmacro wait [ms & body]
  `(js/setTimeout (fn [] ~@body) ~ms))

(defn- run-scenario* [output items]
  (let [stepping-keys #{::on-event ::on-event-name ::wait}]
    (if (empty? items)
      `(do ~@output)
      (let [item (peek items)
            rest (pop items)
            waiting-step? (and (map? item)
                               (-> (keys item)
                                   set
                                   (set/intersection stepping-keys)
                                   not-empty))
            not-latest-step? (not-empty rest)]
        (cond
          (not waiting-step?)
          (recur (conj output item) rest)
          ;; ---
          (and waiting-step?
               not-latest-step?
               (-> (count item) (= 1))
               (contains? item ::on-event)
               )
          `(do ~@(conj output `(utils.scenario/do-on-event*
                                 ~(get item ::on-event)
                                 (fn [] ~(run-scenario* [] rest)))))
          ;; ---
          (and waiting-step?
               not-latest-step?
               (-> (count item) (= 1))
               (contains? item ::on-event-name))
          `(do ~@(conj output `(utils.scenario/do-on-event-name*
                                 ~(get item ::on-event-name)
                                 (fn [] ~(run-scenario* [] rest)))))
          ;; ---
          (and waiting-step?
               not-latest-step?
               (-> (count item) (= 1))
               (contains? item ::wait))
          `(do ~@(conj output `(js/setTimeout
                                 (fn [] ~(run-scenario* [] rest))
                                 ~(get item ::wait))))
          ;; ---
          :else (throw (Exception. "Illegal state in scenario")))))))

(defmacro run-scenario [& body]
  `(do ~(run-scenario* [] (apply list body))
       :scenario-started))
