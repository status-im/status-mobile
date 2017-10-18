(ns status-im.ui.screens.network-settings.add-rpc.views
  (:require
    [re-frame.core :refer [dispatch]]
    [status-im.ui.components.status-bar :as status-bar]
    [status-im.ui.components.toolbar.view :as toolbar]
    [status-im.ui.components.text-input-with-label.view :refer [text-input-with-label]]
    [status-im.ui.screens.network-settings.views :as network-settings]
    [status-im.ui.components.react :as react]
    [status-im.ui.components.sticky-button :as sticky-button]
    [status-im.i18n :as i18n]
    [clojure.string :as str]))

(defn add-rpc-url []
  (let [rpc-url "text"]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/add-network)]
     [network-settings/network-badge]
     [react/view {:margin-top 8}
      [text-input-with-label {:label (i18n/label :t/rpc-url)}]]
     [react/view {:flex 1}]
     (when (not (str/blank? rpc-url))
       [sticky-button/sticky-button (i18n/label :t/add-network) #()])]))
