(ns status-im.contexts.settings.language-and-currency.events-test
  (:require
    [cljs.test :refer-macros [is]]
    matcher-combinators.test
    status-im.contexts.settings.language-and-currency.events
    [test-helpers.unit :as h]))

(def raw-currency-popular
  {:id          "usd"
   :shortName   "USD"
   :name        "US Dollar"
   :symbol      "$"
   :emoji       "🇺🇸"
   :isPopular   true
   :isToken     false
   :imageSource "https://example.com/image.png"})

(h/deftest-event :settings/get-currencies-success
  [event-id dispatch]
  (let [expected-effects {:db {:currencies {:usd {:id         :usd
                                                  :short-name "USD"
                                                  :symbol     "$"
                                                  :emoji      "🇺🇸"
                                                  :name       "US Dollar"
                                                  :popular?   true
                                                  :token?     false}}}}]
    (is (match? expected-effects (dispatch [event-id [raw-currency-popular]])))))
