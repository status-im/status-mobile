# Codebase Structure and Guidelines

As of Nov/2022, we're starting to document and migrate the codebase to [new
guidelines](new-guidelines.md), which will supersede some of the guidelines in
this document.

## Starting the app

The app is bundled and run in a Nix environment. Information regarding how to run it locally can be found [here](https://status.im/technical/build_status/).

## Business Logic Modules

Each business logic module is managed in a module directory.

**Only the core and db namespace of a module can be required outside of the module directory. Other namespaces must never be required outside of the module directory**

*There is no rigid structure on how to organize code inside modules outside of core and db namespaces*

```txt
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

### core.cljs

Core namespace must only contain functions that can be called outside of the module (i.e. in events or other modules):

- fx producing functions called by events and other modules

    ```clojure
    (def get-current-account
        module.db/get-current-account)

    (defn set-current-account [{db :db :as cofx}]
        {:db (module.db/set-current-account db)})
    ```

### db.cljs

- must contain getter and setter functions used by fx producing functions and subscriptions
- db logic called by other modules

Rationale:

These guidelines make db.cljs namespaces the place to go when making changes to the db layout and minimize breaking changes when adding/refactoring features

## re-frame

### Subscriptions

- all subscriptions must be defined in the single `status-im.subs` namespace
- subscriptions must subscribe only on other subscriptions, and never on app-db itself
- `reg-root-key-sub` should be used for the root keys subscriptions

### Events

- all events must be defined in the `core.cljs` namespaces
- events must always be declared with `fx/defn` macro

## Testing flow
- All PRs automatically go to "REVIEW" column on [Pipeline for QA](https://github.com/status-im/status-mobile/projects/7) project on Github. This is our main board for QA / devs interaction
- After the PR gets at least 1 approval, it should be moved to "E2E Tests" column. Some PRs may need approvals from more than one person.
- Critical path tests are automatically run for all PRs in "E2E Tests" column.
- If E2E tests pass:
  - If PR doesn't need manual QA (it is small change or change doesn't relate to app) - after passing e2e tests it can be merged (don't use the Github UI to merge, check section on merging PRs).

  - If manual QA needed - add label 'request manual qa'. The QA team will process it (make sure the summary and test instructions are clear). QA team will review results and test PR on different platforms and assign one of the three labels:
    - No issues - label 'TESTED-OK' and then you can merge it.
    - If issues - label 'TESTED-ISSUES' and comment from QA with bugs.
    - After fix and/or discussion the process is repeated.

  - If manual QA is not needed but all tests don't pass, you can ping @churik or @Serhy to confirm that failed E2E tests are not unrelated.

## Enabling debug logs
Calls to `log/debug` will not be printed to the console by default. It can be enabled under "Advanced settings" in the app:

![Enable Debug Logs](images/codebase-structure-and-guidelines/log-settings.png)

## Translations
The app relies on system locale to select a language from the [list of supported languages](https://github.com/status-im/status-mobile/blob/bda73867471cf2bb8a68b1cc27c9f94b92d9a58b/src/status_im/i18n_resources.cljs#L9). It falls back to English in cash the system locale is not supported.

We use Lokalise App to manage [translations](https://translate.status.im/). In case you need to add/remove a key to translations, you only need to change `en.json`. Missing keys fallback to `en.json`. The actual translations will be added by Lokalise.

## re-frisk
re-frisk is a state visualization tool written by our very own Andrey (@flexsurfer). To start re-frisk, execute the following command:
```bash
$ yarn shadow-cljs run re-frisk-remote.core/start
```

or you can also use make:

```bash
$ make run-re-frisk
```

A server will be started at http://localhost:4567. It might show "not connected" at first. Don't worry and just start using the app. The events and state will populate.

## Merging approved PRs

We don't Github's UI to merge. Instead `./scripts/merge-pr.sh` is used to sign and merge PR to `develop`. You first need to enable [GPG signing on you commits](https://github.com/status-im/status-mobile/blob/develop/STARTING_GUIDE.md#configure-gpg-keys-for-signing-commits).

Once your commits are verified and PR approved, you can run the script like so:

```bash
$ git checkout develop
$ git fetch
$ git reset --hard origin/develop
$ ./scripts/merge-pr.sh 11370
```
Replace 11370 with your PR id.

## Release process
TODO(shivekkhurana): Compile release notes
