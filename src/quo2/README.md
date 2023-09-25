# Quo Library

Quo is the name of our mobile *component library* that implements the [Status
Design System for Mobile](https://www.figma.com/file/WQZcp6S0EnzxdTL4taoKDv/Design-System-for-Mobile).

The overarching goals of having this component library are:

- Achieve the highest possible fidelity between code and the design system in
  Figma.
- Decouple components from ever-changing business requirements.

> [!NOTE]
> This document captures our current practices and guidelines for implementing
> the design system. For guidelines that apply across the entire project, take
> a look at [new-guidelines](/doc/new-guidelines.md).

## Directory structure and file names

We follow one basic rule: mirror Figma *pages* and their *component names* in
the directory structure.

For example, in the screenshot below we see the Figma page is `Banners`
and the component name is `Banner`.

<img src="/doc/images/quo-component.png" width="600" />

Therefore, the structure should look like:

```
quo2/
└── components/
    └── banners/
        └── banner/
            ├── component_spec.cljs
            ├── style.cljs
            └── view.cljs
```

Files `view.cljs`, `style.cljs`, and `component_spec.cljs` should always have
the same name, regardless of component.

## Component API

Adhere to the **same component properties and values** used in a Figma component
when translating it to Clojure. This means using the same names for props and
the same values. If the Figma property is a boolean, use a question mark suffix
to make the name more idiomatic in Clojure.

We have found over time that the less we drift from the design system the
better. Some key benefits:

- It helps developers quickly check for issues when comparing the code with the
  source of truth in Figma.
- It is easier for pull-request reviewers to double-check components for
  correctness.
- It helps developers create preview screens that are identical or very similar
  to Figma, which aids in spotting bugs more easily.
- It helps designers review all component variations in preview screens.

<img src="/doc/images/figma-properties.png" width="600" />

In the image above we can see the properties are `Type`, `State`, `Size`,
`Icon`, `Theme`, and `Background`. Translated to Clojure:

```clojure
;; ns quo2.components.buttons.button.view
(def view
  [{:keys [type state size icon theme background]}]
  ...)
```

### Handling Sizes
In the designs, sizes are referred to as integers. To avoid having the codebase littered with magic numbers we instead have a keyword convention to use in components to map these keywords with their sizes.

The convention is `:size-<number>`, e.g size `20` is `:size-20`

```clojure
;; bad
(defn button
  [{:keys [size]}]
  [rn/view
   {:style {:height (case size
                      20 20
                      40 40
                      0)}}]
  ...)
```

```clojure
;; good
(defn button
  [{:keys [size]}]
  [rn/view
   {:style {:height (case size
                      :size-20 20
                      :size-40 40
                      0)}}]
  ...)
```

## Clojure var conventions

- Due to the fact that every `view` namespace should export only one component
  and to avoid the redundancy of `[some-component/some-component ...]`, name the
  public var `view` as well.
- Try to make all other vars private because they should almost never be used
  directly.

## Component tests

We don't attempt to write component tests verifying how components look on the
screen. Instead, we have found a middle ground, where the focus is on verifying
if events are triggered as intended and that all component variations are
rendered. We use [React Native Testing Library](https://callstack.github.io/react-native-testing-library/).

There are dozens of examples in the repository, so use them as a reference. A
good and complete example is [quo2.components.avatars.user-avatar.component-spec](/src/quo2/components/avatars/user_avatar/component_spec.cljs)

## Do not couple the library with re-frame

Don't use re-frame inside this library (e.g. dispatch & subscribe). If a
component needs to be stateful, the state should be local to its rendering
lifecycle (using `reagent.core/atom`). Additionally, if the component requires
any other data, it should be passed as arguments.

```clojure
;; bad
(defn view []
  (let [window-width (rf/sub [:dimensions/window-width])]
    [rn/pressable {:on-press #(rf/dispatch [:do-xyz])}
     (do-something window-width)]))

;; good
(defn view [{:keys [window-width on-press]}]
  [rn/pressable {:on-press on-press}
   (do-something window-width)])
```

## Themes

Our goal is to make all design system components *themeable*, which means they
should not use, nor fallback to the OS theme, because themes are *contextual*
and can be overridden in specific parts of the app.

To achieve this, use the higher-order function `quo2.theme/with-theme` to
automatically inject the current theme context (based on the [React Context
API](https://react.dev/learn/passing-data-deeply-with-context)).

Use the following pattern:

```clojure
(ns quo2.components.<figma page>.<component name>.view
  (:require [quo2.theme :as quo.theme]))

(defn- view-internal [{:keys [theme]}]
  ...)

(def view (quo.theme/with-theme view-internal))
```

Then pass the `theme` value down to all functions that may rely on the OS theme,
like `quo2.foundations.colors/theme-colors` or `quo2.foundations.shadows/get`.

## Avoid using quo's version number in namespace aliases

When requiring quo2 namespaces, don't use the version number in the
[alias](https://clojure.org/guides/learn/namespaces#_require), unless for a
special reason you need to require both the old and new namespaces in the same
file.

> [!NOTE]
> Keep in mind that, at the moment, we need to keep both `src/quo/` and
> `src/quo2/` directories in the repository, but eventually the old one will go
> away and the version number will lose its meaning.

```clojure
;; bad
(ns ...
  (require [quo2.theme :as quo2.theme]
           [quo2.core :as quo2]))

;; good
(ns ...
  (require [quo2.theme :as quo.theme]
           [quo2.core :as quo]))
```

## Preview screens

Every component should be accompanied by a preview screen in
`src/status_im2/contexts/quo_preview/`. Ideally, **all possible variations in
Figma should be achievable in the preview screen** by changing the input values
without resorting to code changes. Designers will also use this capability to
review components in PR builds.

## Allow outermost containers to have their styles overridden

If a component needs to be wrapped in a `rn/view` instance to force it to be
styled differently, consider changing the component to accept a
`container-style` argument. This will help reduce the number of nodes to be
rendered.

```clojure
;; bad
[rn/view {:style {:margin-right 12}}
 [quo/button
  {:size 32}
  :i/info]]

;; good
[quo/button
 {:size            32
  :container-style {:margin-right 12}}
 :i/info]
```
