(ns status-im2.contexts.shell.jump-to.constants)

(def ^:const shell-animation-time 200)
(def ^:const switcher-card-size 160)
(def ^:const floating-shell-button-height 44)

;; Bottom tabs
(def ^:const bottom-tabs-container-height-android 57)
(def ^:const bottom-tabs-container-height-ios 82)
(def ^:const bottom-tabs-container-extended-height-android 90)
(def ^:const bottom-tabs-container-extended-height-ios 120)
(def ^:const bottom-tab-width 90)

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

;; Home stack states
(def ^:const close-without-animation 0)
(def ^:const open-without-animation 1)
(def ^:const close-with-animation 2)
(def ^:const open-with-animation 3)

;; Switcher Cards
(def ^:const empty-card 0)
(def ^:const one-to-one-chat-card 1)
(def ^:const private-group-chat-card 2)
(def ^:const community-card 3)
(def ^:const community-channel-card 4)
(def ^:const browser-card 5)
(def ^:const wallet-card 6)
(def ^:const wallet-collectible 7)
(def ^:const wallet-graph 8)
(def ^:const communities-discover 9)

;; Floating Screens
(def ^:const community-screen :community-overview)
(def ^:const chat-screen :chat)

(def ^:const floating-screens [chat-screen community-screen])

;; Floating Screen states
(def ^:const close-screen-without-animation 0)
(def ^:const open-screen-without-animation 1)
(def ^:const close-screen-with-slide-animation 2)
(def ^:const open-screen-with-slide-animation 3)
(def ^:const close-screen-with-shell-animation 4)
(def ^:const open-screen-with-shell-animation 5)

;; Floating Screen gesture
(def ^:const gesture-width 30)
(def ^:const gesture-fling-right-velocity 2000)
(def ^:const gesture-fling-left-velocity -1000)
