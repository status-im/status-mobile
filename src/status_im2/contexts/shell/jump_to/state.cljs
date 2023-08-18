(ns status-im2.contexts.shell.jump-to.state
  (:require [reagent.core :as reagent]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]))

;; Atoms
(def selected-stack-id (atom nil))
(def screen-height (atom nil))
(def shared-values-atom (atom nil))
(def jump-to-list-ref (atom nil))

(def home-stack-state (atom shell.constants/close-with-animation))
(def floating-screens-state (atom {}))

;; Reagent atoms used for lazily loading home screen tabs
(def load-communities-stack? (reagent/atom false))
(def load-chats-stack? (reagent/atom false))
(def load-wallet-stack? (reagent/atom false))
(def load-browser-stack? (reagent/atom false))

;NOTE temporary while we support old wallet
(def load-new-wallet? (reagent/atom false))
