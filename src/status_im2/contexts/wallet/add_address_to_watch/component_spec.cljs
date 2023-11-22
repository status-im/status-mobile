(ns status-im2.contexts.wallet.add-address-to-watch.component-spec
  (:require
    [re-frame.core :as re-frame]
    [status-im2.contexts.wallet.add-address-to-watch.view :as add-address-to-watch]
    [test-helpers.component :as h]))

(defn setup-subs
  [subscriptions]
  (doseq [keyval subscriptions]
    (re-frame/reg-sub
     (key keyval)
     (fn [_] (val keyval)))))

(h/describe "select address for watch only account"
  (h/test "validation messages show for already used addressed"
    (setup-subs {:wallet/scanned-address              nil
                 :wallet/addresses                    (set
                                                       ["0x12E838Ae1f769147b12956485dc56e57138f3AC8"
                                                        "0x22E838Ae1f769147b12956485dc56e57138f3AC8"])
                 :wallet/watch-address-activity-state nil
                 :profile/customization-color         :blue})
    (h/render [add-address-to-watch/view])
    (h/is-falsy (h/query-by-label-text :error-message))
    (h/fire-event :change-text
                  (h/get-by-label-text :add-address-to-watch)
                  "0x12E838Ae1f769147b12956485dc56e57138f3AC8")
    (h/is-truthy (h/get-by-translation-text :address-already-in-use))))

(h/test "validation messages show for invalid address"
  (setup-subs {:wallet/scanned-address              nil
               :wallet/addresses                    (set
                                                     ["0x12E838Ae1f769147b12956485dc56e57138f3AC8"
                                                      "0x22E838Ae1f769147b12956485dc56e57138f3AC8"])
               :wallet/watch-address-activity-state nil
               :profile/customization-color         :blue})
  (h/render [add-address-to-watch/view])
  (h/is-falsy (h/query-by-label-text :error-message))
  (h/fire-event :change-text (h/get-by-label-text :add-address-to-watch) "0x12E838Ae1f769147b")
  (h/is-truthy (h/get-by-translation-text :invalid-address)))

