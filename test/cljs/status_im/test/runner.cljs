(ns status-im.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [status-im.test.chat.events]
            [status-im.test.contacts.events]
            [status-im.test.accounts.events]
            [status-im.test.wallet.events]
            [status-im.test.profile.events]
            [status-im.test.chat.models.input]
            [status-im.test.components.main-tabs]
            [status-im.test.handlers]
            [status-im.test.utils.utils]
            [status-im.test.utils.money]
            [status-im.test.utils.clocks]
            [status-im.test.utils.erc20]
            [status-im.test.utils.random]
            [status-im.test.utils.gfycat.core]
            [status-im.test.utils.signing-phrase.core]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests
 'status-im.test.chat.events
 'status-im.test.accounts.events
 'status-im.test.contacts.events
 'status-im.test.profile.events
 'status-im.test.wallet.events
 'status-im.test.chat.models.input
 'status-im.test.components.main-tabs
 'status-im.test.handlers
 'status-im.test.utils.utils
 'status-im.test.utils.money
 'status-im.test.utils.clocks
 'status-im.test.utils.erc20
 'status-im.test.utils.random
 'status-im.test.utils.gfycat.core
 'status-im.test.utils.signing-phrase.core)
