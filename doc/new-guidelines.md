# Guidelines

>The goal of this document is to help all contributors (core and external) to
>write code in _unison_ and help establish good practices that serve the Status
>Mobile contributors well.

We don't want to turn this document into an exhaustive list of rules to be
followed that nobody will read. As much as possible, we'll try to document only
what we consider important for Status Mobile. In other words, we don't want to
maintain a general Clojure convention/style guide, nor do we want to turn this
document into a long tutorial.

This is a **work in progress**, and not all conventions are properly implemented
in the codebase yet. The project structure is also going over major changes (as
of Nov/2022), and it'll take a considerable amount of time until we migrate the
existing code to the new structure.

If you find out anything is outdated or missing, please, share with us or even
better, create a pull-request! 🤸

## Style guide

We mostly follow the [Clojure Style
Guide](https://github.com/bbatsov/clojure-style-guide), so it's recommended to
get familiar with it.

As of Nov/2022, running `make lint` should fix the most basic formatting issues,
and we are in the process of integrating a tool to format non-trivial code
according to the Clojure Style Guide. This should greatly reduce noise in
pull-request reviews and it'll simplify the life of all contributors.

Pay special attention to:

- Align let bindings https://github.com/bbatsov/clojure-style-guide#bindings-alignment
- Align map keys https://github.com/bbatsov/clojure-style-guide#map-keys-alignment

## Dos and don'ts

### Component styles

Prefer to define styles in separate files named `style.cljs`, usually colocated
with the source file using it. For a real example, see
[src/status_im/ui2/screens/chat/messages/style.cljs](../src/status_im/ui2/screens/chat/messages/style.cljs).

```clojure
;; bad
(defn animated-checkbox-view [{:keys [size]}]
  [animated/view
   {:style {:width           size
            :height          size
            :border-radius   4
            :justify-content :center
            :align-items     :center}}
   [animated/view
    (do-something)]])

;; good
(defn animated-checkbox-view [{:keys [size]}]
  [animated/view
   {:style (style/animated-checkbox-style size)}
   [animated/view
    (do-something)]])
```

### Using TODOs comments

_TODO_ comments are used extensively in the codebase, but prefer to use them
only when strictly necessary and when an issue is not enough to track the work
left to be done.

These are all good examples:

```clojure
;; TODO(@username): <message>
;; TODO(@username): <message>, <issue URL>
;; TODO(YYYY-MM-DD): <message>
;; TODO(@username,YYYY-MM-DD): <message>
```

### Subscription names and event names

Always register events and subscriptions using a meaningful namespace, but don't
namespace them with `::`. We understand it's a controversial decision because
there are both pros and cons to such practice.

Whenever appropriate, it's also recommended to use _fake_ namespaces to convey
more knowledge in the keyword about which bounded context (domain) it refers to.
You may also use dots to convey hierarchical structures.

```clojure
;; bad
;; Don't use real namespaced keywords.
(re-frame/reg-sub
 ::profile-pictures-visibility
 :<- [:multiaccount]
 (fn [multiaccount]
   (:profile-pictures-visibility multiaccount)))

;; good
;; Uses a fake namespaced keyword.
(re-frame/reg-sub
 :profile/pictures-visibility
 :<- [:multiaccount]
 (fn [multiaccount]
   (:profile-pictures-visibility multiaccount)))

;; better
;; Uses a fake namespaced keyword with a parent namespace (multiaccount).
(re-frame/reg-sub
 :multiaccount.profile/pictures-visibility
 :<- [:multiaccount]
 (fn [multiaccount]
   (:profile-pictures-visibility multiaccount)))
```

### Declaring view components

Use the simple `defn` to declare components. Don't use `utils.views/defview` and
`utils.views/letsubs`.

```clojure
;; bad
(utils.views/defview browser []
  (utils.views/letsubs [window-width [:dimensions/window-width]]
    (do-something window-width)))

;; good
(defn browser []
  (let [window-width (rf/sub [:dimensions/window-width])]
    (do-something window-width)))
```

### Using re-frame subscriptions and dispatching events

Use the `utils.re-frame` namespace instead of `re-frame.core` to subscribe and
dispatch.

```clojure
;; bad
(ns my-namespace
  (:require [re-frame.core :as rf]))

(let [username @(rf/subscribe [:username])]
  [pressable/pressable {:on-press #(rf/dispatch [:do-something])}
   [rn/view
    (str "Hello " username)]])

;; good
(ns my-namespace
  (:require [utils.re-frame :as rf]))

(let [username (rf/sub [:username])]
  [pressable/pressable {:on-press #(rf/dispatch [:do-something])}
   [rn/view
    (str "Hello " username)]])
```

### Registering event handlers

Events must always be declared with the `utils.fx/defn` macro. Also, don't use
`re-frame.core/reg-event-db`.

```clojure
;; bad
(re-frame/reg-event-fx
 :wakuv2.ui/save-all-confirmed
 (fn [{:keys [db] :as cofx}]
   ...))

;; good
(fx/defn save-all
  {:events [:wakuv2.ui/save-all-confirmed]}
  [{:keys [db] :as cofx}]
  ...)
```

### Registering top-level re-frame subscriptions

Use `subs.root/reg-root-key-sub` to register top-level (root) subscriptions.
Additionally, register root subscriptions in the `subs.root` namespace.

```clojure
;; bad
(re-frame/reg-sub
 :view-id
 (fn [db]
   (:view-id db)))

;; good
(reg-root-key-sub :view-id :view-id)
```

### Registering layer-3 subscriptions

The majority of the subscriptions should be defined as [layer-3
subscriptions](https://day8.github.io/re-frame/subscriptions/#the-four-layer)
due to performance constraints.

```clojure
;; bad
(re-frame/reg-sub
 :ens/preferred-name
 (fn [db]
   (get-in db [:multiaccount :preferred-name])))

;; good
(re-frame/reg-sub
 :ens/preferred-name
 :<- [:multiaccount]
 (fn [multiaccount]
   (:preferred-name multiaccount)))
```

### Requiring quo2 components

Never directly import a submodule of `quo2`, instead, use the provided
`quo2.core` namespace.

```clojure
;; bad
(ns my-namespace
  (:require [quo2.components.icon :as icon]))

(icon/icon :i/verified)

;; good
(ns my-namespace
  (:require [quo2.core :as quo2]))

(quo2/icon :i/verified)
```

### Require/import

Prefer `:as` instead of `:refer`. There are exceptions to this rule, e.g. the
test macros `deftest` and `is`, which are ubiquitous in the Clojure community.

```clojure
;; bad
(ns status-im.utils.datetime
  (:require [cljs-time.coerce :refer [from-long]]))

;; good
(ns status-im.utils.datetime
  (:require [cljs-time.coerce :as time.coerce]))
```

### Accessibility labels

Use keywords instead of strings. As a bonus, remember keywords are cached in
memory.

```clojure
;; bad
[text/text {:accessibility-label "profile-nickname"}
 "Markov"]

;; good
[text/text {:accessibility-label :profile-nickname}
 "Markov"]
```

### Icons

Use the appropriate keyword qualification/namespace and don't directly require
namespaces inside `quo2/components/`.

```clojure
;; bad
(require '[quo2.components.icon :as icons])
(icons/icon :main-icons2/verified)

;; good
(require '[quo2.core :as quo2])
(quo2/icon :i/verified)
```

### Translations

Prefer to use translation placeholders instead of creating multiple translation
keywords and concatenating them into a single string.

```clojure
;; bad
;; Assume the translation key is:
;;   "biometric-auth-error": "Unable perform biometric authentication"
(str (i18n/label :t/biometric-auth-error) "(" error-code ")")

;; good
;; Assume the translation key is:
;;   "biometric-auth-error": "Unable perform biometric authentication ({{code}})"
(i18n/label :t/biometric-auth-error {:code error-code})
```

## Project Structure

As the mobile application grows in size and complexity, new patterns emerge to
organize the codebase. The structure presented below is just an example of what
the codebase could look like, so not all pieces are presented here.

```
src
├── common
│  └── bottom_sheet.cljs
├── contexts
│   ├── browser/
│   ├── communities
│   │   ├── components/
│   │   ├── screens
│   │   │   └── __test__
│   │   └── utils
│   │       └── example.cljs
│   ├── messaging/
│   ├── user_settings/
│   └── wallet/
├── quo2
│   ├── components/
│   ├── foundations/
│   ├── screens/
│   └── theme.cljs
├── react_native
│   ├── gesture.cljs
│   └── platform.cljs
├── setup
│   ├── core.cljs
│   ├── events.cljs
│   ├── navigation.cljs
│   ├── reloader.cljs
│   └── test_runner.cljs
├── subs
│   ├── ens.cljs
│   └── pairing.cljs
└── utils
    ├── number.cljs
    └── test_helpers.cljs
```

### Example structure for a quo2 component

Notice the `test.cljs` file does not live inside a `__tests__/` directory, as
this would be overkill in the quo2 component library.

```
src
└── quo2
    ├── components
    │   └── dropdown
    │       ├── style.cljs
    │       ├── test.cljs
    │       └── view.cljs
    └── screens
        └── dropdown
            └── view.cljs
```

### Rationale

- Bringing ideas from [Domain-Driven
  Design](https://en.wikipedia.org/wiki/Domain-driven_design), we want to
  establish a clean separation of concerns between different [bounded
  contexts](https://martinfowler.com/bliki/BoundedContext.html). Directories
  inside `src/contexts/` should be named after particular Status domains.

- Directories named `utils` can appear at any level of the directory tree. The
  directory nesting level precisely indicates its boundaries. For example, a
  `user_settings/utils/datetime.cljs` namespace communicates that the `datetime`
  namespace should only be used in the `user_settings` context.

- Directories named `common` can appear at any level of the directory tree. Just
  like directories named `utils`, their directory nesting level communicates
  their applicable limits.

- Bounded contexts should not directly require each other's namespaces. We don't
  want to make this a hard rule yet, but it's important to respect it as much as
  possible. For example, namespaces inside `messaging` should not require
  namespaces in the `browser` bounded context. In reality, different domains
  should talk to each other using an interface of some kind.

- The `src/setup/` directory contains namespaces that are mostly used to
  initialize the application, configure test runners, etc. In general, such
  namespaces should not be required from the outside.

- The `src/react_native/` directory should contain only low-level constructs to
  help React Native work in tandem with Clojure(Script).

- The `src/quo2` directory holds all components for the new design system. As
  much as possible, its sub-directories and component names should reflect the
  same language used by designers. Even though the directory lives alongside the
  rest of the codebase, we should think of it as an external entity that
  abstracts away particular Status domain knowledge. Additionally, components
  inside `quo2` should not rely on re-frame, i.e. they should not dispatch
  events or use subscriptions.

#### Re-frame

- Subscriptions should live inside the top-level `src/subs/` directory.

- Re-frame event handlers should be defined in files named `events.cljs`, and
  they should be _close_ to other _things_, like view files, components, etc.

For example:

```
src
└── contexts
    └── browser
        ├── bookmarks/
        ├── options/
        ├── permissions/
        ├── events.cljs
        ├── events_test.cljs
        ├── style.cljs
        └── view.cljs
```

#### Tests

[Unit tests](#glossary) should be created alongside their respective source
implementation. We prefer them colocated with the source and not like most
Clojure (JVM) codebases which mirror the sources in a top-level test directory.
Component tests should be created in a separate directory `__tests__`. Example:

```
├── models
│   ├── message.cljs
│   └── message_test.cljs
├── __tests__
│   └── component_bla_test.cljs
├── models.cljs
└── models_test.cljs
```

There's no hard rule on how integration test namespaces should be split, but
we're at least striving to define them under appropriate bounded contexts that
mirror the source code.

```
test
├── appium/
└── integration
    ├── browser/
    ├── communities/
    ├── messaging/
    ├── user_settings/
    └── wallet
        └── payment_test.cljs
```

## Glossary

**Unit test**: The smallest atomic unit that's meaningful to test. For example,
tests for utility functions and event handlers are considered unit tests in the
mobile codebase. They should be completely deterministic, _fast_, and they
should work flawlessly in the REPL.
