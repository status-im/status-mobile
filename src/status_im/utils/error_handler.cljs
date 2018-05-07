(ns status-im.utils.error-handler
  (:require [clojure.string :as string]
            [status-im.utils.utils :as utils]))

;; Error handling code based on https://gist.github.com/pesterhazy/e6846be1b6712a9038537022d131ce46

(defonce !handler-set (atom false))

(defn downgrade-reagent-errors!
  "Downgrade reagent error to warning
  Reagent uses console.error to notify the user that an exception occurs while
  rendering. Unfortunately, react-native can only show one RedBox at a time and
  discards all subsequent ones. This obscures the actual exception. By
  downgrading the first screen to a warning, the second screen is actually shown
  to the user."
  []
  (when-not @!handler-set
    (reset! !handler-set true)
    (let [original-error (.-error js/console)]
      (set! (.-error js/console)
            (fn [& [head :as args]]
              (if (and (string? head) (string/starts-with? head "Error rendering component"))
                (apply (.-warn js/console) "Additional exception info:" args)
                (apply original-error args)))))))

(defn format-error [e]
  (if (instance? js/Error e)
    {:name (.-name e) :message (.-message e) :stack (.-stack e)}
    {:message (pr-str e)}))

(defn handle-error [e is-fatal]
  (let [f (format-error e)]
    (js/console.log (str "PRETTY PRINTED EXCEPTION"
                         "\n\n***\nNAME: "
                         (pr-str (:name f))
                         "\nMESSAGE: "
                         (:message f)
                         "\n\n"
                         (:stack f)
                         "\n\n***"))))

(defonce !error-handler-set? (atom false))

(defn register-exception-handler!
  "Improve error messages printed to console.
   When js/goog.DEBUG is false, show a popup with an error summary; else rely on default `red` screen."
  []
  (downgrade-reagent-errors!)
  (when-not @!error-handler-set?
    (reset! !error-handler-set? true)
    (let [orig-handler (some-> js/ErrorUtils .-getGlobalHandler (.call))]
      (js/ErrorUtils.setGlobalHandler
       (fn [e isFatal]
         (handle-error e isFatal)
         (if js/goog.DEBUG
           (some-> orig-handler (.call nil e isFatal))
           (utils/show-popup "Error" (.-message e))))))))
