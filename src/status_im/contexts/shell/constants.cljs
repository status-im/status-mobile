(ns status-im.contexts.shell.constants)

(def ^:const floating-shell-button-height 44)

(def ^:const default-selected-stack :wallet-stack)

;; Bottom tabs
(def ^:const bottom-tabs-container-height-android 57)
(def ^:const bottom-tabs-container-height-ios 82)

;; Stacks
(def ^:const stacks-ids [:communities-stack :chats-stack :wallet-stack :browser-stack])

;; Keywords
(def ^:const stacks-opacity-keywords
  {:communities-stack :communities-stack-opacity
   :chats-stack       :chats-stack-opacity
   :wallet-stack      :wallet-stack-opacity
   :browser-stack     :browser-stack-opacity})

(def ^:const tabs-icon-color-keywords
  {:communities-stack :communities-tab-icon-color
   :chats-stack       :chats-tab-icon-opacity
   :wallet-stack      :wallet-tab-icon-opacity
   :browser-stack     :browser-tab-icon-opacity})

(def ^:const stacks-z-index-keywords
  {:communities-stack :communities-stack-z-index
   :chats-stack       :chats-stack-z-index
   :wallet-stack      :wallet-stack-z-index
   :browser-stack     :browser-stack-z-index})
