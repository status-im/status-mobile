# Business Logic Modules

Each business logic module is managed in a module directory.

**Only the core and db namespace of a module can be required outside of the module directory. Other namespaces must never be required outside of the module directory**

*There is no rigid structure on how to organize code inside modules outside of core and db namespaces*

```
- events.cljs
- subs.cljs
- notifications
    - core.cljs
- init
    - core.cljs
- accounts
    - core.cljs
    - db.cljs
- node
    - core.cljs
    - db.cljs
    - config.cljs
- ui
    - screens
        - login
            - views.cljs
```

Rationale and how to get there:

Currently a lot of business logic is lost in utils and screens namespaces and should be moved to dedicated top level directories.
This should make core logic of the app more accessible and organized

## core.cljs

Core namespace must only contain functions that can be called outside of the module (i.e. in events or other modules):

- fx producing functions called by events and other modules

```clojure
(def get-current-account 
    module.db/get-current-account)

(defn set-current-account [{db :db :as cofx}] 
    {:db (module.db/set-current-account db)})
```

## db.cljs

- must contain specs for the app-db subpart the module modifies
- must contain getter and setter functions used by fx producing functions and subscriptions
- db logic called by other modules

Rationale:

These guidelines make db.cljs namespaces the place to go when making changes to the db layout and minimize breaking changes when adding/refactoring features

# Events

- all events must be defined in the single `status-im.events` namespace which can be considered as an index of everything going on in the app
- events must only contain a function call defined in a module
```clojure
(handlers/register-handler-fx
 :notifications/handle-push-notification
 (fn [cofx [_ event]]
   (notifications/handle-push-notification event cofx)))
```
- events must use synthetic namespaces:
    - `:module.ui/` for user triggered events
    - `:module.callback/` for callback events, which are events bringing back the result of an fx to the event loop, the name of the event should end with `-success` or `-error` most of the time. Other possibilities can be `-granted`, `-denied` for instance.
    - `:module/` for internal events, examples are time based events marked `-timed-out`, external changes marked `-changed` or reception of external events marked `-received`. 
