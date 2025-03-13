(ns status-im.setup.oops
  (:require [oops.config]))

(defn setup!
  "Change oops defaults to warn and print instead of throwing exceptions during
  development."
  []
  (oops.config/update-current-runtime-config!
   merge
   {:error-reporting              :console
    :expected-function-value      :warn
    :invalid-selector             :warn
    :missing-object-key           :warn
    :object-is-frozen             :warn
    :object-is-sealed             :warn
    :object-key-not-writable      :warn
    :unexpected-empty-selector    :warn
    :unexpected-object-value      :warn
    :unexpected-punching-selector :warn
    :unexpected-soft-selector     :warn}))
