(ns status-im.test.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [status-im.test.chat.events]
            [status-im.test.contacts.events]
            [status-im.test.contacts.subs]
            [status-im.test.accounts.events]
            [status-im.test.data-store.realm.core]
            [status-im.test.wallet.subs]
            [status-im.test.wallet.transactions.subs]
            [status-im.test.wallet.transactions.views]
            [status-im.test.profile.events]
            [status-im.test.bots.events]
            [status-im.test.offline-messaging-settings.events]
            [status-im.test.transport.core]
            [status-im.test.chat.models]
            [status-im.test.chat.models.input]
            [status-im.test.chat.models.message]
            [status-im.test.chat.subs]
            [status-im.test.chat.views.message]
            [status-im.test.i18n]
            [status-im.test.transport.inbox]
            [status-im.test.protocol.web3.inbox]
            [status-im.test.utils.utils]
            [status-im.test.utils.money]
            [status-im.test.utils.handlers-macro]
            [status-im.test.utils.clocks]
            [status-im.test.utils.inbox]
            [status-im.test.utils.ethereum.eip681]
            [status-im.test.utils.ethereum.core]
            [status-im.test.utils.random]
            [status-im.test.utils.gfycat.core]
            [status-im.test.utils.signing-phrase.core]
            [status-im.test.utils.transducers]
            [status-im.test.utils.async]
            [status-im.test.utils.datetime]
            [status-im.test.utils.mixpanel]
            [status-im.test.utils.prices]))

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests
 'status-im.test.utils.async
 'status-im.test.chat.events
 'status-im.test.chat.subs
 'status-im.test.chat.models
 'status-im.test.accounts.events
 'status-im.test.contacts.events
 'status-im.test.contacts.subs
 'status-im.test.profile.events
 'status-im.test.data-store.realm.core
 'status-im.test.offline-messaging-settings.events
 'status-im.test.bots.events
 'status-im.test.transport.core
 'status-im.test.wallet.subs
 'status-im.test.wallet.transactions.subs
 'status-im.test.wallet.transactions.views
 'status-im.test.chat.models.input
 'status-im.test.chat.models.message
 'status-im.test.chat.views.message
 'status-im.test.i18n
 'status-im.test.transport.inbox
 'status-im.test.protocol.web3.inbox
 'status-im.test.utils.utils
 'status-im.test.utils.handlers-macro
 'status-im.test.utils.money
 'status-im.test.utils.clocks
 'status-im.test.utils.inbox
 'status-im.test.utils.ethereum.eip681
 'status-im.test.utils.ethereum.core
 'status-im.test.utils.random
 'status-im.test.utils.gfycat.core
 'status-im.test.utils.signing-phrase.core
 'status-im.test.utils.transducers
 'status-im.test.utils.datetime
 'status-im.test.utils.mixpanel
 'status-im.test.utils.prices)
