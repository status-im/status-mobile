(ns status-im.contexts.shell.utils
  (:require
    [react-native.async-storage :as async-storage]
    [react-native.platform :as platform]
    [status-im.contexts.shell.constants :as shell.constants]
    [status-im.contexts.shell.state :as state]))

(defn bottom-tabs-container-height
  []
  (if platform/android?
    shell.constants/bottom-tabs-container-height-android
    shell.constants/bottom-tabs-container-height-ios))

(defn load-stack
  [stack-id]
  (case stack-id
    :screen/communities-stack (reset! state/load-communities-stack? true)
    :screen/chats-stack       (reset! state/load-chats-stack? true)
    :screen/wallet-stack      (reset! state/load-wallet-stack? true)
    :screen/market-stack      (reset! state/load-market-stack? true)
    :screen/browser-stack     (reset! state/load-browser-stack? true)
    ""))

(defn change-selected-stack
  [stack-id]
  (when stack-id
    (load-stack stack-id)
    (reset! state/selected-stack-id-value stack-id)
    (async-storage/set-item! :selected-stack-id stack-id)))

(defn reset-bottom-tabs
  []
  (let [selected-stack-id @state/selected-stack-id-value]
    (reset! state/load-communities-stack? (= selected-stack-id :screen/communities-stack))
    (reset! state/load-chats-stack? (= selected-stack-id :screen/chats-stack))
    (reset! state/load-wallet-stack? (= selected-stack-id :screen/wallet-stack))
    (reset! state/load-market-stack? (= selected-stack-id :screen/market-stack))
    (reset! state/load-browser-stack? (= selected-stack-id :screen/browser-stack))))
