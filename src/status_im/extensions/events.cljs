(ns status-im.extensions.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.extensions.core :as extensions]
            [status-im.extensions.registry :as extensions.registry]))

(handlers/register-handler-fx
 :extensions.callback/qr-code-scanned
 (fn [cofx [_ _ url]]
   (extensions/set-extension-url-from-qr cofx url)))

(handlers/register-handler-fx
 :extensions.ui/add-extension-pressed
 (fn [cofx [_ extension-key]]
   (extensions/edit cofx extension-key)))

(handlers/register-handler-fx
 :extensions.ui/uninstall-extension-pressed
 (fn [cofx [_ extension-key]]
   (extensions.registry/uninstall cofx extension-key)))

(handlers/register-handler-fx
 :extensions.ui/input-changed
 (fn [cofx [_ input-key value]]
   (extensions/set-input cofx input-key value)))

(handlers/register-handler-fx
 :extensions.ui/activation-checkbox-pressed
 (fn [cofx [_ extension-key active?]]
   (extensions.registry/change-state cofx extension-key active?)))

(handlers/register-handler-fx
 :extensions.ui/find-button-pressed
 (fn [cofx [_ url]]
   (extensions.registry/load cofx url false)))

(handlers/register-handler-fx
 :extensions.ui/install-extension-button-pressed
 (fn [cofx [_ url]]
   (extensions.registry/install-from-message cofx url true)))

(handlers/register-handler-fx
 :extensions.ui/install-button-pressed
 (fn [cofx [_ url data modal?]]
   (extensions.registry/install cofx url data modal?)))
