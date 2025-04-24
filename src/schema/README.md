# Schemas

This document will grow to describe how we use Malli in the project and our
conventions. It's still early days 🐪

## Guidelines
### Use var quote `#'` when aliasing instrumented vars

It is common in this repository to have aliases to vars. For example, `view`
referring to `var-internal`, or `quo.core/button` referring to
`quo.components.buttons.button.view`.

If the original var being aliased is instrumented, the alias var MUST [var
quote](https://clojure.org/guides/weird_characters#_var_quote) the original var.
If you don't do this, the aliased var will not be instrumented.

```clojure
;; bad, view-internal is instrumented, but both aliases don't use a var quote.
(schema.core/=> view-internal ?schema)
(def view (quo.context/with-theme view-internal))
(def button quo.components.buttons.button.view/button)

;; good
(schema.core/=> view-internal ?schema)
(def view (quo.context/with-theme #'view-internal))
(def button #'quo.components.buttons.button.view/button)
```

### Prefix schema references with `?`

Prefix schema bindings and vars with a question mark `?`. This is the naming
convention used by malli itself when functions receive instances of schemas and
it's an unambiguous way to avoid naming clashes.

```clojure
;; bad
(def message-type [:enum ...])

(defn view
  [message-type] ; Shadows `message-type` schema
  (do-something message-type))

;; good
(def ?message-type [:enum ...])

(defn view
  [message-type] ; Unambiguous naming strategy
  (do-something message-type))
```

### Define schemas as functions when needed

Malli has many utility functions to manipulate schemas as data, and they will
automatically check if the schemas were already defined in the registry.

For schemas we want to conveniently access from the global registry, like
`:schema.common/theme`, they must be registered before Malli tries to use them.

```clojure
;; bad, will fail if :schema.common/bar is not registered.
(def ^:private ?foo
  (malli.util/select-keys :schema.common/bar [:id :name]))

;; good, execution will be delayed until the schema ?foo is correctly registered.
(defn- ?foo
  []
  (malli.util/select-keys :schema.common/bar [:id :name]))
```
