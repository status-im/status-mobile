(ns status-im.setup.schema
  (:require
    [malli.core :as malli]
    [malli.dev.cljs :as malli.dev]
    [malli.dev.pretty :as malli.pretty]
    [malli.dev.virhe :as malli.virhe]
    malli.error
    malli.instrument
    malli.util
    schema.common
    [schema.core :as schema]
    schema.re-frame
    schema.registry
    [taoensso.timbre :as log]))

;;;; Formatters
;; These formatters replace the original ones provided by Malli. They are more
;; compact (less line breaks) and don't show the "More Information" section.

(defn block
  "Same as `malli.dev.pretty/-block`, but adds only one line break between `text`
  and `body`."
  [text body printer]
  [:group (malli.virhe/-text text printer) :break [:align 2 body]])

(defmethod malli.virhe/-format ::malli/explain
  [_ _ {:keys [schema] :as explanation} printer]
  {:body
   [:group
    (block "Value:" (malli.virhe/-visit (malli.error/error-value explanation printer) printer) printer)
    :break :break
    (block "Errors:" (malli.virhe/-visit (malli.error/humanize explanation) printer) printer)
    :break :break
    (block "Schema:" (malli.virhe/-visit schema printer) printer)]})

(defmethod malli.virhe/-format ::malli/invalid-input
  [_ _ {:keys [args input fn-name]} printer]
  {:body
   (cond-> [:group
            (block "Invalid function arguments:" (malli.virhe/-visit args printer) printer)
            :break :break]
     fn-name
     (conj (block "Function Var:" (malli.virhe/-visit fn-name printer) printer)
           :break
           :break)
     :else
     (conj (block "Input Schema:" (malli.virhe/-visit input printer) printer)
           :break
           :break
           (block "Errors:" (malli.pretty/-explain input args printer) printer)))})

(defmethod malli.virhe/-format ::malli/invalid-output
  [_ _ {:keys [value args output fn-name]} printer]
  {:body
   (cond-> [:group
            (block "Invalid function return value:" (malli.virhe/-visit value printer) printer)
            :break :break]
     fn-name
     (conj (block "Function Var:" (malli.virhe/-visit fn-name printer) printer)
           :break
           :break)
     :else
     (conj (block "Function arguments:" (malli.virhe/-visit args printer) printer)
           :break
           :break
           (block "Output Schema:" (malli.virhe/-visit output printer) printer)
           :break
           :break
           (block "Errors:" (malli.pretty/-explain output value printer) printer)))})

(defn register-schemas
  "Register all global schemas in `schema.registry/registry`.

  Since keys in a map are unique, remember to qualify keywords. Prefer to add to
  the global registry schemas for domain entities (e.g. message, chat,
  notification, etc) or unambiguously useful schemas, like
  `:schema.common/theme`."
  []
  (schema.registry/merge (malli.util/schemas))
  (schema.common/register-schemas)
  (schema.re-frame/register-schemas))

(defn setup!
  "Configure Malli and initializes instrumentation.

  After evaluating an s-exp in the REPL that changes a function schema you'll
  need to either save the file where the schema is defined and hot reload or
  manually call `setup!`, otherwise you won't see any changes. It is safe and
  even expected you will call `setup!` multiple times in REPLs."
  []
  (try
    (schema.registry/init-global-registry)
    (register-schemas)

    ;; In theory not necessary, but sometimes in a REPL session the dev needs to
    ;; call unstrument! manually.
    (malli.instrument/unstrument!)

    (malli.dev/start! {:report (schema/reporter)})
    (log/debug "Schemas initialized.")

    ;; It is relatively easy to write invalid schemas, but we don't want to
    ;; block the app from initializing if such errors happen, at least not until
    ;; Malli matures in the project.
    (catch js/Error e
      (log/error "Failed to initialize schemas" {:error e}))))
