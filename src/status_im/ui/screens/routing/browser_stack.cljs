(ns status-im.ui.screens.routing.browser-stack
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.screens.browser.empty-tab.views :as empty-tab]
            [status-im.ui.screens.browser.views :as browser]
            [status-im.ui.screens.browser.tabs.views :as browser.tabs]
            [status-im.ui.components.tabbar.styles :as tabbar.styles]))

(defonce stack (navigation/create-stack))

(defn browser-stack []
  [stack {:initial-route-name :empty-tab
          :header-mode        :none}
   [{:name      :empty-tab
     :insets    {:top true}
     :style     {:padding-bottom tabbar.styles/tabs-diff}
     :component empty-tab/empty-tab}
    {:name         :browser
     :back-handler :noop
     :options      {:animationEnabled false}
     :component    browser/browser}
    {:name       :browser-tabs
     :insets     {:top true}
     :component  browser.tabs/tabs}]])
