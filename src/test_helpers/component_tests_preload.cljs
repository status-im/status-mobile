(ns test-helpers.component-tests-preload
  {:dev/always true}
  (:require
    ;; NOTE: Do NOT sort i18n-resources because it MUST be loaded first.
    [status-im.setup.i18n-resources :as i18n-resources]
    #_{:clj-kondo/ignore [:unsorted-required-namespaces]}
    [status-im.setup.interceptors :as interceptors]
    [utils.i18n :as i18n]))

(defn- setup
  "Prerequisites to run some component tests, for example, the ones in
  `status-im.contexts.wallet.send.input-amount.component-spec`.

  Because of the way Jest and ShadowCLJS are set up, this is a preload file that
  should never be directly required. However, it will be loaded automatically
  before any component test runs."
  []
  (interceptors/register-global-interceptors)
  (i18n/set-language "en")
  (i18n-resources/load-language "en"))

(setup)
