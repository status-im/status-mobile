(ns status-im2.contexts.chat.composer.constants
  (:require
    [quo2.foundations.typography :as typography]
    [react-native.platform :as platform]))

(def ^:const bar-container-height 20)

(def ^:const input-height (if platform/ios? 32 42))

(def ^:const actions-container-height 56)

(def ^:const composer-default-height (+ bar-container-height input-height actions-container-height))

(def ^:const multiline-minimized-height (+ input-height 18))

(def ^:const empty-opacity 0.7)

(def ^:const images-container-height 76)

(def ^:const reply-container-height 32)

(def ^:const edit-container-height 32)

(def ^:const mentions-max-height 240)

(def ^:const extra-content-offset (if platform/ios? 6 0))

(def ^:const content-change-threshold 10)

(def ^:const drag-threshold 30)

(def ^:const velocity-threshold -1000)

(def ^:const background-threshold 0.75)

(def ^:const line-height (:line-height typography/paragraph-1))
