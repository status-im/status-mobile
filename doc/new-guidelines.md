# Code Style Guidelines

> [!IMPORTANT]
> The goal of this document is to help all contributors (core and external) to
> write code in _unison_ and help establish good practices that serve the Status
> Mobile contributors well.

We don't want to turn this document into an exhaustive list of rules to be
followed that nobody will read. As much as possible, we'll try to document only
what we consider important for Status Mobile. In other words, we don't want to
maintain a general Clojure convention/style guide, nor do we want to turn this
document into a long tutorial.

> [!WARNING]
> This is a **work in progress**, and not all conventions are properly
> implemented in the codebase yet. The project structure is also undergoing
> major changes, and it will take a considerable amount of time until we migrate
> the existing code to the new structure.

If you find out anything is outdated or missing, please, share with us or even
better, create a pull-request! 🤸

## Style guide

We follow the [Clojure Style
Guide](https://github.com/bbatsov/clojure-style-guide) and we use
[zprint](https://github.com/kkinnear/zprint) to format Clojure code. Running
`make lint-fix` should fix most formatting issues, but not all of them.

## Dos and don'ts

### Hiccup

Never use anonymous inline function in hiccup, this will lead to
reinitialization of component on each render of parent component.

```clojure
;; bad
(defn checkbox-view
  [{:keys [size]}]
  [rn/view
   [(fn [] [rn/view])]])

;; good
(defn comp []
  [rn/view])

(defn checkbox-view
  [{:keys [size]}]
  [rn/view
   [comp]])
```

This mistake mostly happens with functional components.

```clojure
;; bad
(fn []
  (let [atom (rf/sub [:sub])]
    (fn []
      [:f>
       (fn []
         [rn/text atom]

;; good
(defn f-comp [atom]
 [rn/text atom])

(fn []
 (let [atom (rf/sub [:sub])]
   (fn []
     [:f> f-comp atom])))
```

It's important to name functional components with `f-` prefix.

### Component props and API scheme to match Figma as closely as possible

Ideally, the prop names for components (particularly in quo2 Design System)
should match the Figma properties as best as possible. This makes it easier for
the developer using that component to configure it correctly for the screen it
is being used on and avoids unnecessary overwrites and adjustments being made.

#### Avoid unnecessarily grouping categories to reduce the number of props

For example in Figma if there is a component and it has the following variants:

|theme: "light" blur: "False"|theme: "dark" blur: "False"|theme: "light" blur: "True"|theme: "dark"  blur: "True"|
|----------------------------|---------------------------|---------------------------|---------------------------|
| type :neutral label "ABC"  | type :neutral label "ABC" |                           |                           |
| type :active  label "ABC"  | type :active  label "ABC" |                           |                           |
| type :danger  label "ABC"  | type :danger  label "ABC" | type :danger label "ABC"  | type :danger label "ABC"  |

```clojure
;; bad
"theme - :light or :dark
type - can be :neutral :active :danger :danger-blur"
(defn my-component [{:keys [theme type]} label])

;; good
"theme - :light or :dark
 type - can be :neutral :active :danger
 blur? - boolean
"
(defn my-component [{:keys [theme blur? type]} label])
```

Please note this is only for the external API of the component and there should
be no restriction of how the component manages its internal API as that will not
affect the developer using the component with the issues described above.

In some cases this is not always possible or does not make sense. However the
thought process should be how easy will it be for another developer to use this
component with the correct configuration given the screen designs for Figma.

#### Avoid unnecessarily renaming props

In general it can be helpful to avoid renaming props from their counterpart in
Figma.

For example if Figma has sizes `:small`, `:medium` and `:large`

```clojure
;; bad
":size - :little, :default or :big"
(defn my-component [{:keys [size]}])

;; good
":size - :small, :medium or :large"
(defn my-component [{:keys [size]}])
```

### Component styles

Prefer to define styles in a separate file named `style.cljs`, colocated with
the source file. For a real example, see
[src/quo2/components/record_audio/record_audio/style.cljs](../src/quo2/components/record_audio/record_audio/style.cljs).

```clojure
;; bad
(defn checkbox-view
  [{:keys [size]}]
  [rn/view
   {:style {:width           size
            :height          size
            :border-radius   4
            :justify-content :center
            :align-items     :center}}
   [rn/view (do-something)]])

;; good
(defn checkbox-view
  [{:keys [size]}]
  [rn/view {:style (style/checkbox size)}
   [rn/view (do-something)]])
```

### Always add styles inside the `:style` key

Although when compiling ReactNative for mobile some components are able work with
their styles in the top-level of the properties map, prefer to add them inside the
`:style` key in order to separate styles from properties:

```clojure
;; bad
[rn/button {:flex               1
            :padding-vertical   10
            :padding-horizontal 20
            :on-press           #(js/alert "Hi!")
            :title              "Button"}]

;; good
[rn/button {:style    {:flex               1
                       :padding-vertical   10
                       :padding-horizontal 20}
            :on-press #(js/alert "Hi!")
            :title    "Button"}]

;; better
;; (define them in a style ns & place them inside `:style` key)
[rn/button {:style    (style/button)
            :on-press #(js/alert "Hi!")
            :title    "Button"}]
```

Also its fine to keep one liner styles in view

```clojure
;; ok
[rn/view {:style {:flex 1 :padding-top 5}}]
```

### Don't define properties in styles ns

Properties must be set on view level

```clojure
;; bad
{:style         {:position         :absolute
                 :left             0
                 :right            0
                 :bottom           0}
 :blur-amount   30
 :blur-radius   25
 :blur-type     :transparent
 :overlay-color :transparent}

;; good
{:position         :absolute
 :left             0
 :right            0
 :bottom           0}
```

### Apply animated styles in the style file

```clojure
;; bad
(defn circle
  []
  (let [opacity (reanimated/use-shared-value 1)]
    [reanimated/view {:style (reanimated/apply-animations-to-style
                              {:opacity opacity}
                              style/circle-container)}]))

;; good
(defn circle
  []
  (let [opacity (reanimated/use-shared-value 1)]
    [reanimated/view {:style (style/circle-container opacity)}]))
```

### Don't use percents to define width/height

In ReactNative, all layouts use the [flexbox
model](https://reactnative.dev/docs/flexbox), so percentages are unnecessary the
vast majority of the time, don't use them. Check out this great [interactive
flexbox guide](https://www.joshwcomeau.com/css/interactive-guide-to-flexbox/) by
Joshua Comeau.

```clojure
;; bad
[rn/view {:style {:width "80%"}}]

;; good
[rn/view {:style {:padding-horizontal 20}}]
```

### Use a question mark to convey the value is a boolean

The Clojure Style Guide suggests using a question mark only in [predicate
functions](https://guide.clojure.style/#naming-predicates), but nothing is
mentioned about other symbols and keywords. We prefer to extend the convention
to all boolean references.

```clojure
;; bad
(let [is-open? true] ...)
(def flag-is-enabled false)

;; good
(let [open? true] ...)
(def flag-enabled? false)
```

And for keywords too:

```clojure
;; bad
[some-component {:logged-in true}]

;; good
[some-component {:logged-in? true}]
```

### Styles def vs defn

Always use `def` over `defn` if there are no dynamic values. This helps cut the
cost of function calls.

```clojure
;; bad
(defn title-column []
  {:height 56})

;; good
(def title-column
  {:height 56})
```

```clojure
;; bad
(def community-card
  {:background-color (colors/theme-colors colors/white colors/neutral-90)})

;; good
(defn community-card []
  {:background-color (colors/theme-colors colors/white colors/neutral-90)})
```

### Custom Colors

The Status designs have a lot of customization of user and group colors. For
consistency it is best to use `customization-color` as the prop key on pages and
components. This will help easily identify what pages and components in the
application are using customized colors.

```clojure
;; bad
(defn community-card [{keys [custom-color]}]
  ...)

;; good
(defn community-card [{keys [customization-color]}]
  ...)
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

### Use `[]` instead of `()` in Reagent components

- The `()` version [does NOT work with Form-2 and
  Form-3](https://github.com/reagent-project/reagent/blob/master/doc/UsingSquareBracketsInsteadOfParens.md#a-further-significant-why)
  components.
- Components defined with `[]` will be [more efficient at re-render
  time](https://github.com/reagent-project/reagent/blob/master/doc/UsingSquareBracketsInsteadOfParens.md#which-and-why)
  because they're interpreted by Reagent and transformed into distinct React
  components, with their own lifecycle.

```clojure
;; bad
[rn/view
 (message-card message)]

;; good
[rn/view
 [message-card message]]
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

### Registering effects

When registering re-frame effects (`reg-fx`), prefer to expose a data-only
interface because that will allow event handlers to stay pure.

For instance, if an effect needs a `on-success` callback, allow it to receive a
*re-frame event vector*. This approach is used by us in the [json-rpc/call
effect](src/status_im2/common/json_rpc/events.cljs), but also by third-party
effects, such as https://github.com/Day8/re-frame-http-fx. For the complete
rationale, see [PR #15936](https://github.com/status-im/status-mobile/pull/15936).

### Using the effect `:json-rpc/call`

Prefer the pure version of `:json-rpc/call` (no callbacks).

```clojure
;; not as good
(rf/defn accept-contact-request
  {:events [:activity-center.contact-requests/accept]}
  [_ contact-id]
  {:json-rpc/call
   [{:method     "wakuext_acceptContactRequest"
     :params     [{:id contact-id}]
     :on-success #(rf/dispatch [:sanitize-messages-and-process-response %])
     :on-error   #(rf/dispatch [:activity-center.contact-requests/accept-error contact-id %])}]})

;; better
(rf/defn accept-contact-request
  {:events [:activity-center.contact-requests/accept]}
  [_ contact-id]
  {:json-rpc/call
   [{:method     "wakuext_acceptContactRequest"
     :params     [{:id contact-id}]
     :on-success [:sanitize-messages-and-process-response]
     :on-error   [:activity-center.contact-requests/accept-error contact-id]}]})
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

Consume `quo2` components from `quo2.core`, unless the namespace is also inside
the `quo2/` directory.

```clojure
;; bad
(ns my-namespace
  (:require [quo2.components.icon :as icon]))

(icon/icon :i/verified)

;; good
(ns my-namespace
  (:require [quo2.core :as quo]))

(quo/icon :i/verified)

;; also good because both namespaces are inside quo2/
(ns quo2.components.tabs.account-selector
  (:require [quo2.components.markdown.text :as text]))
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

### Javascript interop

Use [binaryage/oops](https://github.com/binaryage/cljs-oops) macros instead of
core interop macros.

```clojure
;; bad
(fn [^js event]
  (.-width (.-nativeEvent event)))

;; good
(require '[oops.core :as  oops])
(fn [event]
  (oops/oget event "nativeEvent.width"))
```

### Accessibility labels

Accessibility labels are currently used only for end-to-end tests. Use keywords
instead of strings (remember keywords are cached).

```clojure
;; bad
[text/text {:accessibility-label "profile-nickname"}
 "Markov"]

;; good
[text/text {:accessibility-label :profile-nickname}
 "Markov"]
```

Avoid dynamic labels, for example to specify an element's index because
[Appium](https://appium.io/) already supports element selection based on
indices.

```clojure
;; bad
[button {:accessibility-label (str "do-something" index)}]

;; good
[button {:accessibility-label :do-something}]
```

### Icons

Use the appropriate keyword qualification/namespace.

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

### Tests
#### Subscription tests

Test [layer-3 subscriptions](https://day8.github.io/re-frame/subscriptions/) by
actually subscribing to them, so reframe's signal graph gets validated too.

```clojure
;; bad
(defn user-recipes
  [[current-user all-recipes location]]
  ...)

(re-frame/reg-sub
 :user/recipes
 :<- [:current-user]
 :<- [:all-recipes]
 :<- [:location]
 user-recipes)

(deftest user-recipes-test
  (testing "builds list of recipes"
    (let [current-user {...}
          all-recipes  {...}
          location     [...]]
      (is (= expected (recipes [current-user all-recipes location]))))))

;; good
(require '[test-helpers.unit :as h])

(re-frame/reg-sub
 :user/recipes
 :<- [:current-user]
 :<- [:all-recipes]
 :<- [:location]
 (fn [[current-user all-recipes location]]
   ...))

(h/deftest-sub :user/recipes
  [sub-name]
  (testing "builds list of recipes"
    (swap! rf-db/app-db assoc
           :current-user {...}
           :all-recipes {...}
           :location [...])
    (is (= expected (rf/sub [sub-name])))))
```

## Project Structure

First, the bird's-eye view with some example ClojureScript files:

```
src
├── js/
├── mocks/
├── quo2
│   ├── components/
│   ├── foundations/
│   └── theme.cljs
├── react_native
│   ├── gesture.cljs
│   └── platform.cljs
├── status_im/
├── status_im2
│   ├── common
│   │   └── components
│   │       └── bottom_sheet.cljs
│   ├── contexts/
│   ├── setup/
│   └── subs/
├── test_helpers/
└── utils.cljs
```

- `src/js`: Raw Javascript files, e.g. React Native Reanimated worklets.
- `src/mocks`: Plumbing configuration to be able to run tests.
- `src/quo2/`: The component library for Status Mobile.
- `src/react_native/`: Contains only low-level constructs to help React Native
  work in tandem with Clojure(Script).
- `src/status_im2/`: Directory where we try to be as strict as possible about
  our guidelines and where we prefer to write code for the new, redesigned
  mobile app.
- `src/status_im/`: Directory containing what we call "old code", not yet
  migrated to new guidelines for the new mobile app.
- `src/status_im2/common/`: Directories named `common` can appear at any level
  of the directory tree. Just like directories named `utils`, their directory
  nesting level communicates their applicable limits.
- `src/status_im2/common/components/`: Contains reusable components that are not
  part of the design system (quo2).
- `src/status_im2/contexts/`: Contains [bounded contexts](#glossary), like
  `browser/`, `messaging/`, etc. As much as possible, _bounded contexts_ should
  not directly require each other's namespaces.
- `src/status_im2/setup/`: Contains namespaces that are mostly used to
  initialize the application, configure test runners, etc. In general, such
  namespaces should not be required from the outside.
- `src/test_helpers/`: Reusable utilities for writing all kinds of tests.
- `src/status_im/subs/`: All subscriptions should live inside it.

Directories named `utils/` can appear at any level of the directory tree. The
directory nesting level precisely indicates its boundaries. For example, a
`contexts/user_settings/utils/datetime.cljs` file communicates that it should
only be used in the `user_settings` context.

### src/quo2

The `src/quo2/` directory holds all components for the new design system. As
much as possible, its sub-directories and component names should reflect the
same language used by designers.

Even though the directory lives alongside the rest of the codebase, we should
think of it as an external entity that abstracts away particular Status domain
knowledge.

Components inside `src/quo2/` should not rely on re-frame, i.e. they should not
dispatch events or use subscriptions.

Example structure:

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

### Re-frame events

Event handlers should be defined in files named `events.cljs`, and they should
be _close_ to other _things_, like view files, components, etc.

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

## Deprecation process

To deprecate a var, add the `:deprecated` metadata and, if necessary, suggest an
alternative.

```clojure
;; Good if there's no better alternative yet, but we want to deprecate it anyway.
(defn ^:deprecated foo
  []
  (bar))

;; Good
(defn foo
  {:deprecated "Use some.namespace/var-name instead."}
  []
  (bar))
```

Please check the [Clojure Style](https://guide.clojure.style/#deprecated) documentation

To reduce visual clutter from deprecated methods in your text editor, consult this [example](https://rider-support.jetbrains.com/hc/en-us/community/posts/4419728641810-How-to-disable-the-the-strike-thru-for-deprecated-methods-in-Javascript-#:~:text=Try%20disabling%20%22Preferences%20%7C%20Editor%20%7C,It%20works%20for%20me). The approach can be adapted for settings in VSCode, Emacs, VIM, and others.
  
### Test structure

[Unit tests](#glossary) should be created alongside their respective source
implementation. We prefer them colocated with the source and not like most
Clojure (JVM) codebases which mirror the sources in a top-level test directory.

```
├── models
│   ├── message.cljs
│   └── message_test.cljs
├── models.cljs
└── models_test.cljs
```

Component tests should be created in the same directory as the source component,
and named as `component_spec.cljs`.


```
└── filter
    ├── component_spec.cljs
    ├── style.cljs
    └── view.cljs
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

**Bounded context**: A logical separation between different domains. It's an
important concept in the [Domain-Driven
Design](https://en.wikipedia.org/wiki/Domain-driven_design) literature. See
[Bounded Context, by Martin
Fowler](https://martinfowler.com/bliki/BoundedContext.html) for an introduction
to the topic.
