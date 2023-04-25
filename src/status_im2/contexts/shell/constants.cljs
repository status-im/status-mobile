(ns status-im2.contexts.shell.constants
  (:require [react-native.platform :as platform]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]))

(def shell-animation-time 200)

(defn bottom-tabs-container-height
  []
  (if platform/android? 57 82))

(defn bottom-tabs-extended-container-height
  []
  (if platform/android? 90 120))

(defn status-bar-offset
  []
  (if platform/android? (safe-area/get-top) 0))

;; status bar height is not included in : the dimensions/window for devices with a notch
;; https://github.com/facebook/react-native/issues/23693#issuecomment-662860819
;; More info - https://github.com/status-im/status-mobile/issues/14633
(defn dimensions
  []
  (let [{:keys [width height]} (rf/sub [:dimensions/window])]
    {:width  width
     :height (if (> (status-bar-offset) 28)
               (+ height (status-bar-offset))
               height)}))

(def stacks-ids [:communities-stack :chats-stack :wallet-stack :browser-stack])

(def stacks-opacity-keywords
  {:communities-stack :communities-stack-opacity
   :chats-stack       :chats-stack-opacity
   :wallet-stack      :wallet-stack-opacity
   :browser-stack     :browser-stack-opacity})

(def stacks-pointer-keywords
  {:communities-stack :communities-stack-pointer
   :chats-stack       :chats-stack-pointer
   :wallet-stack      :wallet-stack-pointer
   :browser-stack     :browser-stack-pointer})

(def tabs-icon-color-keywords
  {:communities-stack :communities-tab-icon-color
   :chats-stack       :chats-tab-icon-opacity
   :wallet-stack      :wallet-tab-icon-opacity
   :browser-stack     :browser-tab-icon-opacity})

;; Home stack states

(def ^:const close-with-animation 0)
(def ^:const open-with-animation 1)
(def ^:const close-without-animation 3)
(def ^:const open-without-animation 4)

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
