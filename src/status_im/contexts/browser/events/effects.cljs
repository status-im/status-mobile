(ns status-im.contexts.browser.events.effects
  (:require [re-frame.core :as rf]
            [react-native.view-shot :as view-shot]
            [react-native.webview :as webview]))

(rf/reg-fx :fx.browser/send-message
 (fn [[webview-ref message]]
   (webview/post-message webview-ref message)))
