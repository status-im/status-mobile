(ns status-im.ui.screens.wallet.core
  (:require-macros [status-im.modules :as modules]))

(modules/defmodule wallet
  {:collectibles             'status-im.ui.screens.wallet.collectibles.views/collectibles-list
   :contact-code             'status-im.ui.screens.wallet.components.views/contact-code
   :recent-recipients        'status-im.ui.screens.wallet.components.views/recent-recipients
   :recipient-qr-code        'status-im.ui.screens.wallet.components.views/recipient-qr-code
   :send-assets              'status-im.ui.screens.wallet.components.views/send-assets
   :request-assets           'status-im.ui.screens.wallet.components.views/request-assets
   :wallet                   'status-im.ui.screens.wallet.main.views/wallet
   :onboarding               'status-im.ui.screens.wallet.onboarding.views/screen
   :onboarding-modal         'status-im.ui.screens.wallet.onboarding.views/modal
   :request-transaction      'status-im.ui.screens.wallet.request.views/request-transaction
   :send-transaction-request 'status-im.ui.screens.wallet.request.views/send-transaction-request
   :send-transaction-modal   'status-im.ui.screens.wallet.send.views/send-transaction-modal
   :send-transaction         'status-im.ui.screens.wallet.send.views/send-transaction
   :settings-hook            'status-im.ui.screens.wallet.settings.views/settings-hook
   :manage-assets            'status-im.ui.screens.wallet.settings.views/manage-assets
   :sign-message-modal       'status-im.ui.screens.wallet.sign-message.views/sign-message-modal
   :transaction-fee          'status-im.ui.screens.wallet.transaction-fee.views/transaction-fee
   :transaction-sent-modal   'status-im.ui.screens.wallet.transaction-sent.views/transaction-sent-modal
   :transaction-sent         'status-im.ui.screens.wallet.transaction-sent.views/transaction-sent
   :transactions             'status-im.ui.screens.wallet.transactions.views/transactions
   :transaction-details      'status-im.ui.screens.wallet.transactions.views/transaction-details
   :filter-history           'status-im.ui.screens.wallet.transactions.views/filter-history
   :add-custom-token         'status-im.ui.screens.wallet.custom-tokens.views/add-custom-token
   :custom-token-details     'status-im.ui.screens.wallet.custom-tokens.views/custom-token-details
   :separator                'status-im.ui.screens.wallet.components.views/separator})

(defn collectibles []
  [(get-symbol :collectibles)])

(defn contact-code []
  [(get-symbol :contact-code)])

(defn recent-recipients []
  [(get-symbol :recent-recipients)])

(defn recipient-qr-code []
  [(get-symbol :recipient-qr-code)])

(defn send-assets []
  [(get-symbol :send-assets)])

(defn request-assets []
  [(get-symbol :request-assets)])

(defn wallet []
  [(get-symbol :wallet)])

(defn onboarding []
  [(get-symbol :onboarding)])

(defn onboarding-modal []
  [(get-symbol :onboarding-modal)])

(defn request-transaction []
  [(get-symbol :request-transaction)])

(defn send-transaction-request []
  [(get-symbol :send-transaction-request)])

(defn send-transaction-modal []
  [(get-symbol :send-transaction-modal)])

(defn send-transaction []
  [(get-symbol :send-transaction)])

(defn settings-hook []
  [(get-symbol :settings-hook)])

(defn manage-assets []
  [(get-symbol :manage-assets)])

(defn sign-message-modal []
  [(get-symbol :sign-message-modal)])

(defn transaction-fee []
  [(get-symbol :transaction-fee)])

(defn transaction-sent-modal []
  [(get-symbol :transaction-sent-modal)])

(defn transaction-sent []
  [(get-symbol :transaction-sent)])

(defn transactions []
  [(get-symbol :transactions)])

(defn transaction-details []
  [(get-symbol :transaction-details)])

(defn filter-history []
  [(get-symbol :filter-history)])

(defn add-custom-token []
  [(get-symbol :add-custom-token)])

(defn custom-token-details []
  [(get-symbol :custom-token-details)])

(defn separator []
  [(get-symbol :separator)])
