(ns status-im.ui.screens.routing.browser-stack)

(def browser-stack
  {:name       :browser-stack
   :screens    [:open-dapp
                :browser]
   :config     {:initialRouteName :open-dapp}})