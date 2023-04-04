(ns status-im2.contexts.chat.messages.bottom-sheet-composer.constants
  (:require [react-native.platform :as platform]))

(def ^:const handle-container-height 20)

(def ^:const input-height (if platform/ios? 32 44))

(def ^:const actions-container-height 56)

(def ^:const composer-default-height (+ handle-container-height input-height actions-container-height))

(def ^:const images-container-height 76)

(def ^:const top-gradient-height 80)

(def ^:const drag-threshold 30)

(def ^:const ios-extra-offset 12)

(def ^:const velocity-threshold -1000)
