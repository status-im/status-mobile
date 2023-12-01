(ns utils.scenario
  (:require [clojure.set :as set]))

(defmacro do-on-event
  "Takes a full event and a body to execute when the event happens.
  Example:
  (do (rf/dispatch [:create-profile {:name \"foo\" :password \"bar\"}])
      (do-on-event [:profile-created {:status :ok}]
        (rf/dispatch [:navigate :home])))"
  [event & body]
  `(utils.scenario/do-on-event* ~event (fn [] ~@body)))

(defmacro do-on-event-name
  "Takes the first element of an event (i.e. its name) and a body to execute
  when the event happens.
  Example:
  (do (rf/dispatch [:create-profile {:name \"foo\" :password \"bar\"}])
      (do-on-event-name :profile-created
        (rf/dispatch [:navigate :home])))"
  [event-name & body]
  `(utils.scenario/do-on-event-name* ~event-name (fn [] ~@body)))

(defmacro wait
  "Takes a duration in milliseconds and a body to execute. Uses setTimeout internally.
  Example:
  (do (rf/dispatch [:navigate :options])
      (wait 1000
        (rf/dispatch [:display :popup])))"
  [ms & body]
  `(js/setTimeout (fn [] ~@body) ~ms))

(defn- run-scenario*
  "Implementation function. Should not be used directly."
  [output items]
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

(defmacro run-scenario
  "Takes a body of expressions interleaved with {::on-event [:foo :bar]},
  {::on-event-name :name} and {::wait 1000} markers. `::on-event` takes a full
  event, `::on-event-name` only the first item of an event (i.e. the name), and
  `::wait` takes a time in milliseconds.
  Here is a toy example:
  (run-scenario
    (rf/dispatch [:event1 42])
    {::wait 1000}
    (rf/dispatch [:navigate :home])
    {::wait 1000}
    (rf/dispatch [:create-profile :foo])
    {::on-event-name :profile-created}
    {::wait 1000}
    (rf/dispatch [:choose-avatar :bar])
    {::on-event [:avatar-created [:status :ok]]}
    {::wait 1000}
    (rf/dispatch [:navigate :profile]))"
  [& body]
  `(do ~(run-scenario* [] (apply list body))
       :scenario-started))
