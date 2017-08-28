(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :as rn]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.ui.screens.wallet.send.styles :as cst]))

(defn toolbar-view []
  [toolbar/toolbar2 {:style cst/toolbar}
   [toolbar/nav-button (act/back-white act/default-handler)]
   [toolbar/content-title {:color :white} "Send Transaction"]])

(defview send-transaction []
  []
  [rn/view {:style cst/wallet-container}
   [toolbar-view]
   [rn/text "Nothing here yet!"]])
