(ns legacy.status-im.ui.screens.browser.stack
  (:require
    [legacy.status-im.ui.screens.browser.empty-tab.views :as empty-tab]
    [legacy.status-im.ui.screens.browser.tabs.views :as tabs]
    [legacy.status-im.ui.screens.browser.views :as browser]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [utils.re-frame :as rf]))

(defn browser-stack
  []
  (let [screen-id (rf/sub [:browser/screen-id])]
    [rn/view {:padding-top (safe-area/get-top) :flex 1}
     (case screen-id
       :empty-tab    [empty-tab/empty-tab]
       :browser      [browser/browser]
       :browser-tabs [tabs/tabs]
       [empty-tab/empty-tab])]))
