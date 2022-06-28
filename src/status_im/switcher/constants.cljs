(ns status-im.switcher.constants
  (:require [status-im.utils.handlers :refer [<sub]]
            [status-im.utils.platform :as platform]))

;; For translucent status bar, dimensions/window also includes status bar's height,
;; this offset is used for correctly calculating switcher position
(def switcher-height-offset 24)

;; extra height of switcher container for show/peek hidden cards while opening animation
(def switcher-container-height-padding 100)

(def switcher-button-radius 24)

(def switcher-button-size
  (* switcher-button-radius 2))

(def switcher-pressed-scale 0.9)

(def switcher-pressed-radius
  (* switcher-pressed-scale switcher-button-radius))

(def switcher-pressed-size
  (* 2 switcher-pressed-radius))

(def switcher-bottom-positions
  {:android
   {:home-stack 15
    :chat       57}
   :ios
   {:home-stack 40
    :chat       67}})

(defn switcher-bottom-position [view-id]
  (get-in
   switcher-bottom-positions
   [(keyword platform/os) view-id]))

(defn switcher-pressed-bottom-position [view-id]
  (+
   (get-in
    switcher-bottom-positions
    [(keyword platform/os) view-id])
   (- switcher-button-radius switcher-pressed-radius)))

;; TODO(parvesh) - use different height for android and ios(Confirm from Design)
(defn bottom-tabs-height []
  (if platform/android? 55 80))

(defn dimensions []
  (<sub [:dimensions/window]))

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

(def tabs-opacity-keywords
  {:communities-stack :communities-tab-opacity
   :chats-stack       :chats-tab-opacity
   :wallet-stack      :wallet-tab-opacity
   :browser-stack     :browser-tab-opacity})
