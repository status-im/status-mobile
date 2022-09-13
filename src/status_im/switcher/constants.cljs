(ns status-im.switcher.constants
  (:require [quo.react-native :as rn]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.utils.platform :as platform]
            [quo2.foundations.colors :as colors]))

;; For translucent status bar(android), dimensions/window also includes status bar's height,
;; this offset is used for correctly calculating switcher position
(def switcher-height-offset
  (if platform/android? (:status-bar-height @rn/navigation-const) 0))

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
    :chat       140}
   :ios
   {:home-stack 40
    :chat       140}})

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
  (let [{:keys [width height]} (<sub [:dimensions/window])]
    {:width  width
     :height (+ height switcher-height-offset)}))

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

;; Tabs

(def switcher-header-tab-padding-horizontal 10)

(def switcher-tabs-data [{:name "Switch" :color colors/switcher-background-opa-20}
                         {:name "Scan" :color colors/switcher-background-opa-40}
                         {:name "Share" :color colors/switcher-background-opa-20}
                         {:name "Activity" :color colors/switcher-background-opa-20}])