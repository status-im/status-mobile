(ns status-im.contexts.wallet.add-address-to-watch.component-spec
  (:require
    [status-im.contexts.wallet.add-address-to-watch.view :as add-address-to-watch]
    status-im.contexts.wallet.events
    [test-helpers.component :as h]))

(h/describe "select address for watch only account"
  (h/setup-restorable-re-frame)

  (h/before-each
   (fn []
     (h/setup-subs {:wallet/scanned-address              nil
                    :wallet/addresses                    #{"0x12E838Ae1f769147b12956485dc56e57138f3AC8"
                                                           "0x22E838Ae1f769147b12956485dc56e57138f3AC8"}
                    :wallet/watch-address-activity-state nil
                    :profile/customization-color         :blue})))

  (h/test "validation messages show for already used addressed"
    (h/render [add-address-to-watch/view])
    (h/is-falsy (h/query-by-label-text :error-message))
    (h/fire-event :change-text
                  (h/get-by-label-text :add-address-to-watch)
                  "0x12E838Ae1f769147b12956485dc56e57138f3AC8")
    (h/is-truthy (h/get-by-translation-text :t/address-already-in-use)))

  (h/test "validation messages show for invalid address"
    (h/render [add-address-to-watch/view])
    (h/is-falsy (h/query-by-label-text :error-message))
    (h/fire-event :change-text (h/get-by-label-text :add-address-to-watch) "0x12E838Ae1f769147b")
    (h/is-truthy (h/get-by-translation-text :t/invalid-address))))
