(ns status-im2.contexts.wallet.create-account.edit-derivation-path.component-spec
  (:require
    [status-im2.contexts.wallet.create-account.edit-derivation-path.view :as edit-derivation-path]
    [test-helpers.component :as h]))

(h/describe "Edit derivation path page"
  (h/test "Default render"
    (h/render [edit-derivation-path/view {}])
    (h/is-truthy (h/get-by-translation-text :t/edit-derivation-path))
    (h/is-truthy (h/get-by-translation-text :t/path-format))
    (h/is-truthy (h/get-by-translation-text :t/derivation-path))
    (h/is-truthy (h/get-by-translation-text :t/reveal-address))
    (h/is-truthy (h/get-by-translation-text :t/save)))


  (h/test "Reveal address pressed"
    (let [on-reveal (h/mock-fn)]
      (h/render [edit-derivation-path/view {:on-reveal on-reveal}])
      (h/fire-event :press (h/get-by-translation-text :t/reveal-address))
      (h/was-called on-reveal)
      (h/wait-for #(h/is-truthy (h/get-by-translation-text :t/address-activity)))))

  (h/test "Reset button pressed"
    (let [on-reset (h/mock-fn)]
      (h/render [edit-derivation-path/view {:on-reset on-reset}])
      (h/fire-event :press (h/get-by-translation-text :t/reset))
      (h/was-called on-reset)
      (h/wait-for #(h/is-truthy (h/get-by-translation-text :t/derive-addresses))))))
