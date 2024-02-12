(ns status-im.contexts.shell.jump-to.state
  (:require
    [reagent.core]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]))

;; Atoms
(def selected-stack-id (atom nil))
(def screen-height (atom nil))
(def shared-values-atom (atom nil))
(def jump-to-list-ref (atom nil))

(def home-stack-state (atom shell.constants/close-with-animation))
(def floating-screens-state (atom {}))

;; Reagent atoms used for lazily loading home screen tabs
(def load-communities-stack? (reagent.core/atom false))
(def load-chats-stack? (reagent.core/atom false))
(def load-wallet-stack? (reagent.core/atom false))
(def load-browser-stack? (reagent.core/atom false))
