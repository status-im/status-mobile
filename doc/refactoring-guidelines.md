This document details steps to improve codebase intelligibility. Codebase should be as simple and intuitive as possible to understand and navigate.
It goes in pair with a best practices document that will follow.

status: RFC

To reduce potential for regression and simplify implementation and subsequent merge into develop this refactoring can be split into multiple PRs

# Pull request #1 Cosmetic

This PR only moves things around and rename things so it should not introduce any behavior changes

## Introduce new hierarchy

Reorganize folders / rename file to match new hierarchy and match re-frame semantic.

```
src/status-im/
├── components
├── data_store
├── platforms
├── protocol
├── translations
├── ui
│   └── screens
│       ├── account
│       │   ├── login
│       │   └── recover
│       │       ├── core.cljs
│       │       ├── db.cljs
│       │       ├── events.cljs
│       │       ├── subs.cljs
│       │       └── views.cljs
│       ├── contacts
│       │   └── new_contact
│       │       └── views.cljs
│       └── navigation.cljs
└── utils
```
and
```
resources
├── default_contacts.json
├── default_contacts_groups.json
├── images/
 | └── contacts/
├── bots/
 | ├── browse/
 | ├── ...
 | └── console/
└── vendors/
└── js/
```

- if you have specs in a specs.cljs file move them to the db.cljs file

## Apply code conventions

Fix code convention according to best practices.

https://github.com/bbatsov/clojure-style-guide
http://tonsky.me/blog/readable-clojure/

- Use long namespace aliases (i.e use `string` rather than `str` or `s` for `clojure.string`)
- Use `react` alias instead of directly refering to components in views

The important thing is that by following such guidelines the way to refer to and name things should be more consistent accross the codebase.

### Add letsubs macro in views

#### For emacs users

You can use the following emacs-lisp expression in your .emacs to have proper indentation with the letsubs macro

```emacs-lisp
  (put-clojure-indent 'letsubs 1)
```

#### For cursive users

### Write docstring for important functions

# Pull request #2 Functional

## Rewrite events 

### Use fx and cofx
### Move navigation/preload-data! to navigation.cljs

## Refactor app-db

Make sure app-db is 

- properly normalized
- using namespaced keys
- speced
- documented.

From @janherich

Cases which I find problematic:

Data under top level keys :chat-animations, :chat-loaded-callbacks, :chat-ui-props and :chats should me merged together, as they are all indexed by chat-id, I propose top level key :chat-by-id
Messages should be kept at top level key :message-by-id, every occurrence where message is referenced (for example [:chat-by-id "console" :messages] will contain collection of references to particular messages, like [:message-by-id "intro-message"] we can then either lookup data manually in subscriptions by using reference path as simple (get-in db ...) argument, or maybe even provide some automatic denormalisation where all references (links) will be replaced be referenced data in subscriptions, I'm not sure how that's feasible in reagent subscription model.

## Rewrite subs

Use re-frame best practices:

- db can be used only in extraction subscriptions


## Write tests

# Pull request #3 Misc

## Consolidate namespaces

Merge utils namespaces and reduce files number to improve code understandability.
Consolidate android and ios shared code.

## Improve modularization

Make sure we don't have unwanted dependencies between namespaces (i.e. re-frame usage in non ui/ namespaces)
Might require spliting files


## Follow re-frame patterns

Reorganize ui code by views and follow re-frame patterns

