(ns status-im.ui.screens.routing.browser-stack
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.screens.browser.open-dapp.views :as open-dapp]
            [status-im.ui.screens.browser.views :as browser]))

(defonce stack (navigation/create-stack))

(defn browser-stack []
  [stack {:initial-route-name :open-dapp
          :header-mode        :none}
   [{:name      :open-dapp
     :component open-dapp/open-dapp}
    {:name         :browser
     :back-handler :noop
     :options      {:animationEnabled false}
     :component    browser/browser}]])
