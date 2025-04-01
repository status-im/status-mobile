(ns status-im.contexts.shell.constants)

(def ^:const floating-shell-button-height 44)

(def ^:const default-selected-stack :screen/wallet-stack)

;; Bottom tabs
(def ^:const bottom-tabs-container-height-android 57)
(def ^:const bottom-tabs-container-height-ios 82)

;; Stacks
(def ^:const stacks-ids
  [:screen/communities-stack :screen/chats-stack :screen/wallet-stack :screen/browser-stack])

;; Keywords
(def ^:const stacks-opacity-keywords
  {:screen/communities-stack :communities-stack-opacity
   :screen/chats-stack       :chats-stack-opacity
   :screen/wallet-stack      :wallet-stack-opacity
   :screen/browser-stack     :browser-stack-opacity})

(def ^:const tabs-icon-color-keywords
  {:screen/communities-stack :communities-tab-icon-color
   :screen/chats-stack       :chats-tab-icon-opacity
   :screen/wallet-stack      :wallet-tab-icon-opacity
   :screen/browser-stack     :browser-tab-icon-opacity})

(def ^:const stacks-z-index-keywords
  {:screen/communities-stack :communities-stack-z-index
   :screen/chats-stack       :chats-stack-z-index
   :screen/wallet-stack      :wallet-stack-z-index
   :screen/browser-stack     :browser-stack-z-index})
