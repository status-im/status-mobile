(ns status-im.test-helpers
  "Utilities for simplifying the process of writing tests and improving test
  readability.

  Avoid coupling this namespace with particularities of the Status' domain, thus
  prefer to use it for more general purpose concepts, such as the re-frame event
  layer."
  (:require-macros status-im.test-helpers)
  (:require [re-frame.core :as rf]
            [re-frame.db :as rf-db]
            [re-frame.events :as rf-events]
            [re-frame.registrar :as rf-registrar]
            [re-frame.subs :as rf-subs]
            [taoensso.timbre :as log]))

(defn db
  "A simple wrapper to get the latest value from the app db."
  []
  @rf-db/app-db)

(defn register-helper-events
  "Register utility events for testing.

  Note that re-frame-test removes such events if they're declared in the scope
  of the macro `day8.re-frame.test/run-test-sync` (or the async variant)."
  []
  (rf/reg-event-db
   :test/assoc-in
   (fn [db [_ keys value]]
     (assoc-in db keys value))))

(defn spy-event-fx
  "Re-register event effect using id `id`, but conj to `state` the event
  arguments before calling the original handler.

  Callers of this function can later on deref `state` to make assertions.

  It's recommended to run this function in the scope of the macro
  `day8.re-frame.test/run-test-sync` (or the async variant) as they
  automatically clean up effects."
  [state id]
  (let [interceptors (get-in @rf-registrar/kind->id->handler [:event id])]
    (rf-events/register
     id
     (concat (butlast interceptors)
             (list {:id     :test/spy-event-fx
                    :before (fn [context]
                              (swap! state conj {:id id :args (get-in context [:coeffects :event])})
                              context)}
                   (last interceptors))))))

(defn spy-fx
  "Re-register effect using id `id`, but conj to `state` the effect arguments
  before calling the original effect handler.

  Callers of this function can later on inspect `state` to make assertions.

  It's recommended to run this function in the scope of the macro
  `day8.re-frame.test/run-test-sync` (or the async variant) as they
  automatically clean up effects."
  [state id]
  (let [original-fn (get-in @rf-registrar/kind->id->handler [:fx id])]
    (rf/reg-fx
     id
     (fn [fx-args]
       (swap! state conj {:id id :args fx-args})
       (original-fn fx-args)))))

(defn stub-fx-with-callbacks
  "Re-register effect using id `id` with a no-op version.

  This function is useful to redefine effects that expect callbacks, usually to
  pass downstream dummy data to successful/failure events. In re-frame parlance,
  such effects accept on-success and on-error keywords.

  The original effect handler for `id` is expected to receive a single map as
  argument with either :on-success or :on-error keywords.

  This function expects to receive either `on-success` or `on-error`, but not
  both. If both are passed, `on-error` will be preferred."
  [id & {:keys [on-success on-error]}]
  (rf/reg-fx
   id
   (fn [[fx-map]]
     (let [original-on-error   (:on-error fx-map)
           original-on-success (:on-success fx-map)]
       (cond (and original-on-error on-error)
             (original-on-error (on-error fx-map))
             (and original-on-success on-success)
             (original-on-success (on-success fx-map)))))))

(defn using-log-test-appender
  "Rebinds `taoensso.timbre/*config*` to use a custom test appender that persists
  all `taoensso.timbre/log` call arguments. `f` is called with the atom
  reference so that tests can de-reference it and verify log messages and their
  respective levels."
  [f]
  (let [logs (atom [])]
    (binding [log/*config* (assoc-in log/*config*
                                     [:appenders :test]
                                     {:enabled? true
                                      :fn       (fn [{:keys [vargs level]}]
                                                  (swap! logs conj {:args vargs :level level}))})]
      (f logs))))

(defn restore-app-db
  "Saves current app DB, calls `f` and restores the original app DB.

  Always clears the subscription cache after calling `f`."
  [f]
  (rf-subs/clear-subscription-cache!)
  (let [original-db @rf-db/app-db]
    (try
      (f)
      (finally
        (reset! rf-db/app-db original-db)))))
