(ns status-im.ui.screens.browser.stack
  (:require [utils.re-frame :as rf]
            [status-im.ui.screens.browser.empty-tab.views :as empty-tab]
            [status-im.ui.screens.browser.views :as browser]
            [status-im.ui.screens.browser.tabs.views :as tabs]))

(defn browser-stack
  []
  (let [screen-id (rf/sub [:browser/screen-id])]
    (case screen-id
      :empty-tab    [empty-tab/empty-tab]
      :browser      [browser/browser]
      :browser-tabs [tabs/tabs]
      [empty-tab/empty-tab])))
