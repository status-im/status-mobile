(ns status-im.contexts.shell.state
  (:require
    [reagent.core :as reagent]
    [status-im.contexts.shell.constants :as shell.constants]))

;; Atoms
(def selected-stack-id-value (atom shell.constants/default-selected-stack))
(def selected-stack-id-shared-value (atom nil))

;; Reagent atoms used for lazily loading home screen tabs
(def load-communities-stack? (reagent/atom false))
(def load-chats-stack? (reagent/atom false))
(def load-wallet-stack? (reagent/atom false))
(def load-browser-stack? (reagent/atom false))
