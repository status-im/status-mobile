(ns status-im.setup.test-preload
  (:require
    ["bignumber.js" :as BigNumber]
    [matcher-combinators.core :as matcher-combinators]
    [matcher-combinators.model :as matcher.model]
    [status-im.setup.oops :as setup.oops]))

;; We must implement Matcher in order for tests to work with the `match?`
;; directive.
(extend-type BigNumber
 matcher-combinators/Matcher
   (-matcher-for [this] this)
   (-base-name [_] 'bignumber)
   (-match [this actual]
     (if (-equiv this actual)
       {:matcher-combinators.result/type  :match
        :matcher-combinators.result/value actual}
       {:matcher-combinators.result/type  :mismatch
        :matcher-combinators.result/value (matcher.model/->Mismatch this actual)})))

(setup.oops/setup!)
